package dev.bautista.spotlight.search

import dev.bautista.spotlight.control.SearchScope

data class ModifiedSearch(val query:String,val scope:SearchScope)
object SearchModifiers {
    fun parse(input:String):ModifiedSearch? {
        val q=input.trim();val head=normalize(q.substringBefore(' '));val tail=q.substringAfter(' ',"").trim()
        if(normalize(q) in setOf("aplicaciones de musica","apps de musica"))return ModifiedSearch("",SearchScope("","apps","music"))
        val section=when(head){"/apps"->"apps";"/files"->"files-mode";"/pdf"->"files";"/contacts"->"contacts";"/commands"->"commands";"/ai"->"ai";else->null}
        if(section!=null)return ModifiedSearch(tail,SearchScope("",section,if(head=="/pdf")"pdf" else ""))
        if(head in setOf("contactos","contacto") && tail.isNotBlank())return ModifiedSearch(tail,SearchScope("","contacts"))
        return null
    }
}
