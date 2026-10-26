package dev.bautista.spotlight.data

import org.json.*

/** Visual-only transfer. Unknown fields are rejected; providers, keys and histories cannot enter it. */
object ThemeTransfer {
    private val keys=setOf("actionOpacity","settingsOpacity","windowScale","theme","preset","accent","appearance","opacity","corner","density","textScale","iconSize","width","maxHeight","glassIntensity","surfaceTone","borderWidth","shadowSize","typeface","selectionStyle","animations","blur","movement")
    fun encode(c:Config):String=JSONObject().put("format","spotlight-theme").put("version",1).put("visual",JSONObject()
        .put("actionOpacity",c.actionOpacity.toDouble()).put("settingsOpacity",c.settingsOpacity.toDouble()).put("windowScale",c.windowScale).put("theme",c.theme).put("preset",c.preset).put("accent",c.accent).put("appearance",c.appearance).put("opacity",c.opacity.toDouble()).put("corner",c.corner).put("density",c.density)
        .put("textScale",c.textScale.toDouble()).put("iconSize",c.iconSize).put("width",c.width).put("maxHeight",c.maxHeight).put("glassIntensity",c.glassIntensity.toDouble()).put("surfaceTone",c.surfaceTone)
        .put("borderWidth",c.borderWidth.toDouble()).put("shadowSize",c.shadowSize.toDouble()).put("typeface",c.typeface).put("selectionStyle",c.selectionStyle).put("animations",c.animations).put("blur",c.blur).put("movement",c.movement)).toString(2)
    fun decode(raw:String,c:Config):Config {
        require(raw.length<=16000){"Tema demasiado grande."}
        val root=JSONObject(raw);require(root.keys().asSequence().toSet()==setOf("format","version","visual") && root.getString("format")=="spotlight-theme" && root.getInt("version")==1)
        val j=root.getJSONObject("visual");require(j.keys().asSequence().all{it in keys}){"El tema contiene campos no visuales."}
        val booleans=setOf("animations","blur")
        val numbers=setOf("actionOpacity","settingsOpacity","windowScale","opacity","corner","textScale","iconSize","width","maxHeight","glassIntensity","borderWidth","shadowSize")
        j.keys().asSequence().forEach { key ->
            val value=j.get(key)
            require(when(key){in booleans -> value is Boolean;in numbers -> value is Number;else -> value is String}){"Tipo no compatible: $key"}
        }
        fun s(k:String,old:String,allowed:Set<String>)=j.optString(k,old).also{require(it in allowed){"Valor no compatible: $k"}}
        fun f(k:String,old:Float,min:Float,max:Float)=j.optDouble(k,old.toDouble()).toFloat().also{require(it.isFinite() && it in min..max){"Valor fuera de límites: $k"}}
        fun n(k:String,old:Int,min:Int,max:Int)=f(k,old.toFloat(),min.toFloat(),max.toFloat()).also{require(it%1f==0f)}.toInt()
        return c.copy(actionOpacity=f("actionOpacity",c.actionOpacity,.70f,1f),settingsOpacity=f("settingsOpacity",c.settingsOpacity,.70f,1f),windowScale=f("windowScale",c.windowScale,-1f,1f).also{require(it==-1f || it in 0f..1f)},theme=s("theme",c.theme,setOf("auto","light","dark")),preset=s("preset",c.preset,setOf("glass","pure","hybrid","macos","ios","raycast","graphite")),accent=s("accent",c.accent,setOf("blue","graphite","purple","rose","green","orange")),appearance=s("appearance",c.appearance,setOf("spotlight","raycast")),opacity=f("opacity",c.opacity,.70f,1f),corner=n("corner",c.corner,12,32),density=s("density",c.density,setOf("auto","compact","balanced","spacious")),textScale=f("textScale",c.textScale,.9f,1.3f),iconSize=n("iconSize",c.iconSize,24,36),width=if(j.optInt("width",c.width)==0)0 else n("width",c.width,380,1100),maxHeight=n("maxHeight",c.maxHeight,320,700),glassIntensity=f("glassIntensity",c.glassIntensity,0f,1f),surfaceTone=s("surfaceTone",c.surfaceTone,setOf("neutral","warm","cool")),borderWidth=f("borderWidth",c.borderWidth,0f,2f),shadowSize=f("shadowSize",c.shadowSize,0f,20f),typeface=s("typeface",c.typeface,setOf("inter","system","mono")),selectionStyle=s("selectionStyle",c.selectionStyle,setOf("glass","solid")),animations=j.optBoolean("animations",c.animations),blur=j.optBoolean("blur",c.blur),movement=s("movement",c.movement,setOf("precise","liquid_precision","liquid_immersive")))
    }
    /** Canonical visual-only collection. No URI grants, executable intents or private fields. */
    fun portableThemes(raw:String):JSONArray {
        require(raw.length<=400_000){"Colección de temas demasiado grande."}
        val input=JSONArray(raw);require(input.length()<=20){"Máximo 20 temas."}
        val ids=mutableSetOf<String>()
        return JSONArray().apply {
            for(i in 0 until input.length()) {
                val entry=input.getJSONObject(i)
                require(entry.keys().asSequence().toSet()==setOf("id","name","theme")){"Tema personalizado inválido."}
                val id=entry.get("id");val name=entry.get("name")
                require(id is String && id.length in 1..80 && id.none{it.isISOControl()} && ids.add(id)){"Identificador de tema inválido o duplicado."}
                require(name is String && name.trim().length in 1..50 && name.none{it.isISOControl()}){"Nombre de tema inválido."}
                val visual=decode(entry.getJSONObject("theme").toString(),Config())
                put(JSONObject().put("id",id).put("name",name.trim()).put("theme",JSONObject(encode(visual))))
            }
        }
    }
    fun save(c:Config,name:String):Config {
        require(name.trim().length in 1..50)
        val list=runCatching{JSONArray(c.customThemes)}.getOrDefault(JSONArray())
        require(list.length()<20){"Máximo 20 temas."}
        list.put(JSONObject().put("id",java.util.UUID.randomUUID().toString()).put("name",name.trim()).put("theme",JSONObject(encode(c))))
        return c.copy(customThemes=list.toString())
    }
}
