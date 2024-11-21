package com.jjv360.skadivm.logic

import android.content.Context
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.net.URL
import java.nio.charset.Charset

/** Represents a template that can be used to create a new VM. */
@Serializable
data class VMTemplate(

    /** Template unique ID */
    var id: String,

    /** Template name */
    var name: String,

    /** Template description */
    var description: String?,

    /** Icon URL to display */
    var icon: String?,

    /** List of requirements in order to use this VM */
    var requires: List<String>?,

    /** Custom properties specified in the yaml config */
    var props: Map<String, String>?,

    /** Tasks to run when installing the VM */
    var installTasks: List<String>?,

    /** Tasks to run when launching the VM */
    var runTasks: List<String>,

) {

    /** Statics */
    companion object {

        /** Load templates from YAML config */
        fun fromYaml(input: InputStream): Collection<VMTemplate> {
            return Yaml.default.decodeFromStream<Map<String, VMTemplate>>(input).values
        }

        /** Load templates from YAML config */
        fun fromYaml(yamlString: String): Collection<VMTemplate> {
            return fromYaml(yamlString.byteInputStream(Charset.forName("UTF-8")))
        }

        /** Load templates from YAML config */
        fun fromYaml(yamlFile: File): Collection<VMTemplate> {
            return fromYaml(FileInputStream(yamlFile))
        }

        /** Load templates from remote YAML config */
        fun fromYaml(ctx: Context, url: URL): Collection<VMTemplate> {
            if (url.protocol == "file" && url.path.startsWith("/android_asset/"))
                return fromYaml(ctx.assets.open(url.path.substring(15)))
            else
                return fromYaml(url.openStream())
        }

        /** Save to yaml string */
        fun toYaml(templates: Map<String, VMTemplate>): String {
            return Yaml.default.encodeToString(templates)
        }

    }

}