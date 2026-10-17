package dev.bautista.spotlight.semantic

/** Cased BERT WordPiece. Offsets always refer to the original UTF-16 source. */
class WordPiece(vocabulary:List<String>) {
    data class Piece(val id:Long,val start:Int,val end:Int,val continuation:Boolean=false)
    data class Tokens(val pieces:List<Piece>,val truncated:Boolean)
    data class Fragment(val index:Int,val start:Int,val end:Int,val text:String,val ids:LongArray)
    data class Fragments(val items:List<Fragment>,val truncated:Boolean)
    private val vocab=vocabulary.withIndex().associate{it.value to it.index.toLong()}
    private val unknown=requireNotNull(vocab["[UNK]"])
    private val cls=requireNotNull(vocab["[CLS]"])
    private val sep=requireNotNull(vocab["[SEP]"])
    private val special=Regex("\\[(?:PAD|UNK|CLS|SEP|MASK)\\]")
    init {require(vocabulary.size in 100..200000 && vocab.size==vocabulary.size)}
    fun encode(text:String):LongArray=wrap(tokenize(text,126).pieces.map{it.id})
    private fun wrap(ids:List<Long>)=(listOf(cls)+ids+sep).toLongArray()
    fun fragments(text:String,max:Int=8):Fragments {
        require(max in 1..8)
        val tokens=tokenize(text,max*80+16)
        val out=mutableListOf<Fragment>();var position=0
        while(position<tokens.pieces.size && out.size<max) {
            while(position<tokens.pieces.size && tokens.pieces[position].continuation)position++
            if(position==tokens.pieces.size)break
            var end=minOf(position+96,tokens.pieces.size)
            while(end>position && end<tokens.pieces.size && tokens.pieces[end].continuation)end--
            if(end==position){end=minOf(position+126,tokens.pieces.size);while(end<tokens.pieces.size && tokens.pieces[end].continuation)end++}
            val window=tokens.pieces.subList(position,end)
            val startOffset=window.first().start;val endOffset=window.last().end
            out+=Fragment(out.size,startOffset,endOffset,text.substring(startOffset,endOffset),wrap(window.map{it.id}))
            if(end==tokens.pieces.size)break
            position=maxOf(position+1,end-16)
        }
        return Fragments(out,tokens.truncated || (out.lastOrNull()?.end ?: 0)<(tokens.pieces.lastOrNull()?.end ?: 0))
    }
    fun tokenize(text:String,maxPieces:Int=126):Tokens {
        require(text.length<=100000 && maxPieces in 1..1000)
        val result=mutableListOf<Piece>();var truncated=false
        fun word(value:String,offsets:List<Int>) {
            if(value.isEmpty())return
            val pieces=mutableListOf<Piece>();var start=0
            if(value.codePointCount(0,value.length)>100)pieces+=Piece(unknown,offsets.first(),offsets.last()+1)
            else while(start<value.length) {
                var end=value.length;var found:Long?=null
                while(end>start) {
                    found=vocab[(if(start>0)"##" else "")+value.substring(start,end)]
                    if(found!=null)break
                    end=value.offsetByCodePoints(end,-1)
                }
                if(found==null){pieces.clear();pieces+=Piece(unknown,offsets.first(),offsets.last()+1);break}
                pieces+=Piece(found,offsets[start],offsets[end-1]+1,start>0);start=end
            }
            for(piece in pieces)if(result.size<maxPieces)result+=piece else truncated=true
        }
        var buffer=StringBuilder();val offsets=mutableListOf<Int>()
        fun flush(){word(buffer.toString(),offsets);buffer=StringBuilder();offsets.clear()}
        var i=0
        while(i<text.length) {
            if(result.size>=maxPieces){flush();if(i<text.length)truncated=true;break}
            val added=if(text[i]=='[')special.matchAt(text,i) else null
            if(added!=null){flush();if(result.size<maxPieces)result+=Piece(requireNotNull(vocab[added.value]),i,added.range.last+1) else truncated=true;i=added.range.last+1;continue}
            val cp=text.codePointAt(i);val length=Character.charCount(cp);val type=Character.getType(cp)
            when {
                cp==0 || cp==0xfffd || (type in setOf(Character.CONTROL.toInt(),Character.FORMAT.toInt()) && cp !in listOf(9,10,13))->Unit
                cp in listOf(32,9,10,13) || type==Character.SPACE_SEPARATOR.toInt()->flush()
                punctuation(cp,type) || chinese(cp)->{flush();word(String(Character.toChars(cp)),(i until i+length).toList())}
                else->{buffer.appendCodePoint(cp);repeat(length){offsets+=i+it}}
            }
            i+=length
        }
        flush()
        return Tokens(result,truncated)
    }
    private fun punctuation(cp:Int,type:Int)=cp in 33..47 || cp in 58..64 || cp in 91..96 || cp in 123..126 || type in setOf(Character.CONNECTOR_PUNCTUATION.toInt(),Character.DASH_PUNCTUATION.toInt(),Character.START_PUNCTUATION.toInt(),Character.END_PUNCTUATION.toInt(),Character.INITIAL_QUOTE_PUNCTUATION.toInt(),Character.FINAL_QUOTE_PUNCTUATION.toInt(),Character.OTHER_PUNCTUATION.toInt())
    private fun chinese(cp:Int)=cp in 0x4e00..0x9fff || cp in 0x3400..0x4dbf || cp in 0x20000..0x2a6df || cp in 0x2a700..0x2b73f || cp in 0x2b740..0x2b81f || cp in 0x2b820..0x2ceaf || cp in 0xf900..0xfaff || cp in 0x2f800..0x2fa1f
}
