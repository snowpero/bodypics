package com.ninis.camera_sample.data

import io.realm.RealmObject


public open class SavePicItem: RealmObject() {
    public open var date: Long? = null
    public open var path: String? = null

    override fun toString(): String {
        return String.format("date : %s, path : %s", date, path)
    }
}