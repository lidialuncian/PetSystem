package com.example.petsystem.models

import android.os.Parcel
import android.os.Parcelable

data class Feeder(
    val id: String ="",
    val userId: String = "",
    val first_meal_quantity: String = "",
    val first_meal_hour: String = "",
    val second_meal_quantity: String = "",
    val second_meal_hour: String = "",
    val third_meal_quantity: String = "",
    val third_meal_hour: String = ""
): Parcelable{

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )
    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(id)
        writeString(first_meal_quantity)
        writeString(first_meal_hour)
        writeString(second_meal_quantity)
        writeString(second_meal_hour)
        writeString(third_meal_quantity)
        writeString(third_meal_hour)
    }

    companion object CREATOR : Parcelable.Creator<Feeder> {
        override fun createFromParcel(parcel: Parcel): Feeder {
            return Feeder(parcel)
        }

        override fun newArray(size: Int): Array<Feeder?> {
            return arrayOfNulls(size)
        }
    }

}
