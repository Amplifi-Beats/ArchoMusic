package tk.archo.music.data

import android.os.Parcel
import android.os.Parcelable

data class AlbumItem(val title: String?, val artist: String?,
                      val data: String?): Parcelable {
    val itemtitle: String = title!!
    val itemartist: String = artist!!
    val itemdata: String = data!!

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    fun getAlbumTitle(): String {
        return itemtitle
    }

    fun getAlbumArtist(): String {
        return itemartist
    }

    fun getAlbumData(): String {
        return itemdata
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(artist)
        parcel.writeString(data)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AlbumItem> {
        override fun createFromParcel(parcel: Parcel): AlbumItem {
            return AlbumItem(parcel)
        }

        override fun newArray(size: Int): Array<AlbumItem?> {
            return arrayOfNulls(size)
        }
    }
}