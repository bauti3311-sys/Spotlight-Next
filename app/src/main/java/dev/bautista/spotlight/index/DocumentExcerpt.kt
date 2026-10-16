package dev.bautista.spotlight.index

import dev.bautista.spotlight.search.normalize

/** Local lexical context, not an embedding/semantic model or generated summary. */
object DocumentExcerpt {
 fun around(text:String,query:String,limit:Int=3000):String {
  val bounded=text.take(100000)
  val size=limit.coerceIn(80,3000)
  if(bounded.length<=size)return bounded
  val normalized=normalize(bounded)
  val tokens=normalize(query).split(Regex("[^\\p{L}\\p{N}]+"))
   .filter{it.length>=3 && it !in setOf("and","not","pdf","kind","name","autor","author","modified","created")}.distinct().sortedByDescending{it.length}
  val hit=tokens.firstNotNullOfOrNull { token->normalized.indexOf(token).takeIf{it>=0} } ?: 0
  var anchor=hit
  if(normalized.length!=bounded.length && hit>0) {
   var low=0;var high=bounded.length
   while(low<high){val mid=(low+high)/2;if(normalize(bounded.substring(0,mid)).length<hit)low=mid+1 else high=mid}
   anchor=low
  }
  val start=(anchor-size/3).coerceIn(0,(bounded.length-size).coerceAtLeast(0))
  val end=(start+size).coerceAtMost(bounded.length)
  return (if(start>0)"…" else "")+bounded.substring(start,end)+(if(end<text.length)"…" else "")
 }
}
