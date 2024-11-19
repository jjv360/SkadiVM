package com.jjv360.skadivm.utils

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import kotlinx.serialization.Serializable
import java.io.InputStream
import java.nio.charset.Charset

/** Represents a template that can be used to create a new VM. */
@Serializable
data class VMTemplate(

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

    /** List of files to download before creating the template. Key is file name, value is a URL. */
    var files: Map<String, String>?,

    /** Tasks to run when installing the VM */
    var installTasks: List<String>?,

    /** Tasks to run when launching the VM */
    var runTasks: List<String>?,

) {

    /** Statics */
    companion object {

        /** Load templates from YAML config */
        fun fromYaml(yamlString: String): Collection<VMTemplate> {
            return fromYaml(yamlString.byteInputStream(Charset.forName("UTF-8")))
        }

        /** Load templates from YAML config */
        fun fromYaml(input: InputStream): Collection<VMTemplate> {
            return Yaml.default.decodeFromStream<Map<String, VMTemplate>>(input).values
        }

    }

}