package com.jennifer.cookpedia.entity

import android.os.Parcel
import android.os.Parcelable

data class RecipeNote(
    var id: Int? = 0,
    var name: String? = null,
    var description : String? = null,
    var ingridients : String? = null,
    var steps: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeString(ingridients)
        parcel.writeString(steps)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RecipeNote> {
        override fun createFromParcel(parcel: Parcel): RecipeNote {
            return RecipeNote(parcel)
        }

        override fun newArray(size: Int): Array<RecipeNote?> {
            return arrayOfNulls(size)
        }
    }
}
