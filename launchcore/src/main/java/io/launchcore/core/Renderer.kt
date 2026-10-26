package io.launchcore.core

/**
 * Graphics backend used to translate desktop OpenGL into Android GPU work.
 *
 * Each renderer ships as a runtime pack (downloaded on demand, see
 * [RuntimePacks]); the AAR itself bundles none of them.
 */
@UnstableApi
sealed interface Renderer {

    /** ANGLE (Vulkan backend). Default choice; best compatibility on modern devices. */
    data object Angle : Renderer

    /** gl4es 1.14 (GLES wrapper). Lightest option, good for older/weaker GPUs. */
    data object Gl4es114 : Renderer

    /** OSMesa + Zink (Vulkan). Useful where ANGLE misbehaves. */
    data object OsmesaZink : Renderer

    /** A renderer plugin distributed outside the built-in packs (e.g. FCL-style driver APKs). */
    data class Custom(val id: String) : Renderer
}
