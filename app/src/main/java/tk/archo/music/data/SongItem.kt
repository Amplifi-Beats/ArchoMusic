package tk.archo.music.data

import android.os.Parcel
import android.os.Parcelable

data class SongItem(val title: String?, val artist: String?, val album: String?): Parcelable {
    val itemtitle: String = title!!
    val itemartist: String = artist!!
    val itemalbum: String = album!!

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    fun getSongTitle(): String {
        return itemtitle
    }

    fun getSongArtist(): String {
        return itemartist
    }

    fun getSongAlbum(): String {
        return itemalbum
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(artist)
        parcel.writeString(album)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SongItem> {
        override fun createFromParcel(parcel: Parcel): SongItem {
            return SongItem(parcel)
        }

        override fun newArray(size: Int): Array<SongItem?> {
            return arrayOfNulls(size)
        }
    }
}