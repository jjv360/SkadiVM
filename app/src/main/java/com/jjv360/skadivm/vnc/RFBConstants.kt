package com.jjv360.skadivm.vnc

/** RFB protocol constants */
object RFBConstants {

    /** General */
    const val OK = 0
    const val Failed = 1

    /** Security types */
    object SecurityType {
        const val Invalid = 0
        const val None = 1
        const val VNCAuthentication = 2
    }

    /** ClientInit flags */
    object ClientInit {

        /** Exclusive access means the server should not allow any other clients at the same time */
        const val Exclusive = 0

        /** Shared means the server is allowed to have multiple clients connected */
        const val Shared = 1

    }

    /** Message IDs */
    object ClientToServerMessageType {
        const val SetPixelFormat = 0
        const val SetEncodings = 2
        const val FramebufferUpdateRequest = 3
        const val KeyEvent = 4
        const val PointerEvent = 5
        const val ClientCutText = 6
    }

    /** Message IDs */
    object ServerToClientMessageType {
        const val FramebufferUpdate = 0
        const val SetColorMapEntries = 1
        const val Bell = 2
        const val ServerCutText = 3
    }

    /** Encoding types */
    object Encoding {
        const val Raw = 0
        const val CopyRect = 1
        const val RRE = 2   // <-- Obsolete in spec, ZRLE and TRLE are better
        const val Hextile = 5  // <-- Obsolete in spec, ZRLE and TRLE are better
        const val TRLE = 15
        const val ZRLE = 16
        const val PseudoCursor = -239
        const val PseudoDesktopSize = -223
    }

}