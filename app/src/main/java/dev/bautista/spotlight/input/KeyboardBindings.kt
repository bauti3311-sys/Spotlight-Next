package dev.bautista.spotlight.input

import android.view.KeyEvent
import dev.bautista.spotlight.data.Config

data class KeyChord(val key:Int,val ctrl:Boolean=true,val alt:Boolean=false,val shift:Boolean=false) {
    val encoded get()="${if(ctrl)1 else 0}:${if(alt)1 else 0}:${if(shift)1 else 0}:$key"
    val label get()=listOfNotNull(if(ctrl)"Ctrl" else null,if(alt)"Alt" else null,if(shift)"Shift" else null,
        when(key){KeyEvent.KEYCODE_SPACE->"Espacio";in KeyEvent.KEYCODE_A..KeyEvent.KEYCODE_Z->('A'.code+key-KeyEvent.KEYCODE_A).toChar().toString();else->KeyEvent.keyCodeToString(key).removePrefix("KEYCODE_")}).joinToString("+")
    fun matches(e:KeyEvent)=e.keyCode==key && (e.isCtrlPressed||e.isMetaPressed)==ctrl && e.isAltPressed==alt && e.isShiftPressed==shift
    companion object {
        fun decode(raw:String):KeyChord? {
            val v=raw.split(':').map{it.toIntOrNull() ?: return null}
            if(v.size!=4 || v.take(3).any{it !in 0..1})return null
            return KeyChord(v[3],v[0]==1,v[1]==1,v[2]==1)
        }
        fun capture(e:KeyEvent)=KeyChord(e.keyCode,e.isCtrlPressed||e.isMetaPressed,e.isAltPressed,e.isShiftPressed)
    }
}

/** Local palette bindings only; Android/global navigation and text editing remain reserved. */
object KeyboardBindings {
    val titles=linkedMapOf("actions" to "Abrir acciones","expand" to "Expandir resultados","complete" to "Autocompletar explícitamente","clipboard" to "Abrir portapapeles")
    private val defaults=mapOf("actions" to KeyChord(KeyEvent.KEYCODE_K),"expand" to KeyChord(KeyEvent.KEYCODE_M),"complete" to KeyChord(KeyEvent.KEYCODE_SPACE),"clipboard" to KeyChord(KeyEvent.KEYCODE_V,shift=true))
    fun resolve(c:Config,id:String):KeyChord? {
        c.keyBindings.firstOrNull{it.substringBefore('=')==id}?.substringAfter('=')?.let{KeyChord.decode(it)}?.let{return it}
        if(id=="clipboard")return when(c.clipboardShortcut){"off"->null;"ctrl_p"->KeyChord(KeyEvent.KEYCODE_P);else->defaults[id]}
        return defaults[id]
    }
    fun matches(c:Config,id:String,e:KeyEvent)=resolve(c,id)?.matches(e)==true
    fun global(c:Config)=when(c.shortcut){"alt"->KeyChord(KeyEvent.KEYCODE_SPACE,ctrl=false,alt=true);"ctrl"->KeyChord(KeyEvent.KEYCODE_SPACE);else->KeyChord(KeyEvent.KEYCODE_SPACE,alt=true)}
    fun conflict(c:Config,id:String,chord:KeyChord):String? {
        if(!chord.ctrl || chord.alt || chord.key !in (KeyEvent.KEYCODE_A..KeyEvent.KEYCODE_Z)+KeyEvent.KEYCODE_SPACE)
            return "Usá Ctrl con una letra o Espacio; Android puede reservar Alt y las teclas de navegación."
        if(chord.key in listOf(KeyEvent.KEYCODE_A,KeyEvent.KEYCODE_C,KeyEvent.KEYCODE_X,KeyEvent.KEYCODE_Z,KeyEvent.KEYCODE_Y) ||
            chord.key==KeyEvent.KEYCODE_V && !chord.shift ||
            !chord.shift && chord.key in listOf(KeyEvent.KEYCODE_F,KeyEvent.KEYCODE_B,KeyEvent.KEYCODE_R,KeyEvent.KEYCODE_S))
            return "Combinación reservada para edición de texto u otra acción de Spotlight."
        if(chord==global(c))return "Conflicto con el acceso global a Spotlight."
        titles.keys.firstOrNull{it!=id && resolve(c,it)==chord}?.let{return "Conflicto con ${titles[it]}."}
        return null
    }
    fun assign(c:Config,id:String,chord:KeyChord):Config {
        require(id in titles);require(conflict(c,id,chord)==null){conflict(c,id,chord).orEmpty()}
        return c.copy(keyBindings=c.keyBindings.filterNot{it.substringBefore('=')==id}.toSet()+"$id=${chord.encoded}")
    }
    fun validEntries(entries:Set<String>):Boolean=entries.size<=4 && entries.all { entry ->
        entry.substringBefore('=') in titles && KeyChord.decode(entry.substringAfter('=',""))?.let{it.encoded==entry.substringAfter('=')}==true
    } && entries.map{it.substringBefore('=')}.distinct().size==entries.size
}
