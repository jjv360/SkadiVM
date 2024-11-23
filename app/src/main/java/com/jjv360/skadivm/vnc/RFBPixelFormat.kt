package com.jjv360.skadivm.vnc

import java.io.DataInputStream
import java.io.DataOutputStream


/** PixelFormat object */
data class RFBPixelFormat(
    val bitsPerPixel: Int,
    val depth: Int,
    val bigEndian: Boolean,
    val trueColor: Boolean,
    val redMax: Int,
    val greenMax: Int,
    val blueMax: Int,
    val redShift: Int,
    val greenShift: Int,
    val blueShift: Int,
) {

    /** Statics */
    companion object {

        /** Read from stream */
        fun read(input: DataInputStream): RFBPixelFormat {

            // Read it
            val pf = RFBPixelFormat(
                bitsPerPixel = input.readUnsignedByte(),
                depth = input.readUnsignedByte(),
                bigEndian = input.readByte().toInt() != 0,
                trueColor = input.readByte().toInt() != 0,
                redMax = input.readUnsignedShort(),
                greenMax = input.readUnsignedShort(),
                blueMax = input.readUnsignedShort(),
                redShift = input.readUnsignedByte(),
                greenShift = input.readUnsignedByte(),
                blueShift = input.readUnsignedByte(),
            )

            // Read padding
            input.readNBytes(3)
            return pf

        }

    }

    /** Write to stream */
    fun writeTo(output: DataOutputStream) {
        output.writeByte(bitsPerPixel)
        output.writeByte(depth)
        output.writeByte(if (bigEndian) 1 else 0)
        output.writeByte(if (trueColor) 1 else 0)
        output.writeShort(redMax)
        output.writeShort(greenMax)
        output.writeShort(blueMax)
        output.writeByte(redShift)
        output.writeByte(greenShift)
        output.writeByte(blueShift)
    }

}