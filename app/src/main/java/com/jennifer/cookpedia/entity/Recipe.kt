package com.jennifer.cookpedia.entity

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi

data class Recipe(
        var id: Int?,
        var name: String?,
        var photo: String?,
        var description : String?,
        var ingridients : ArrayList<String>?,
        var steps: ArrayList<String>?,
        var isLiked : Boolean
): Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readValue(Int::class.java.classLoader) as? Int,
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.createStringArrayList() as ArrayList<String>,
            parcel.createStringArrayList() as ArrayList<String>,
            parcel.readByte() != 0.toByte()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(name)
        parcel.writeString(photo)
        parcel.writeString(description)
        parcel.writeList(ingridients)
        parcel.writeList(steps)
        parcel.writeByte(if (isLiked) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Recipe> {
        override fun createFromParcel(parcel: Parcel): Recipe {
            return Recipe(parcel)
        }

        override fun newArray(size: Int): Array<Recipe?> {
            return arrayOfNulls(size)
        }
    }
}