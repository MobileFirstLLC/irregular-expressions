package mf.asciitext.gallery

class ArtPiece(
    id: String?,
    name: String?,
    var text: String,
    var category: String,
    var isFullWidth: Boolean,
    var premium: Boolean
) : ArtCategory(id!!, name!!)