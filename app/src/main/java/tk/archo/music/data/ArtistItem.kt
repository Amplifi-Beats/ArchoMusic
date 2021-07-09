package tk.archo.music.data

import android.os.Parcel
import android.os.Parcelable

data class ArtistItem(val title: String?, val data: String?): Parcelable {
    val itemtitle: String = title!!
    val itemdata: String = data!!

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString()
    )

    fun getArtistTitle(): String {
        return itemtitle
    }

    fun getArtistData(): String {
        return itemdata
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(data)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ArtistItem> {
        override fun createFromParcel(parcel: Parcel): ArtistItem {
            return ArtistItem(parcel)
        }

        override fun newArray(size: Int): Array<ArtistItem?> {
            return arrayOfNulls(size)
        }
    }
}