package com.nikhil.try_out_furnitureapp

import android.Manifest
import com.google.ar.sceneform.ux.ArFragment

class CustomArFragment : ArFragment() {

    override fun getAdditionalPermissions(): Array<String> {
        val additionalPermission = super.getAdditionalPermissions()
        val permissionLength = additionalPermission.size
        val permissions = Array(permissionLength + 1) { Manifest.permission.WRITE_EXTERNAL_STORAGE }

        if (permissionLength > 0) {
            System.arraycopy(additionalPermission, 0 , permissions, 1, permissionLength)
        }
        return permissions
    }
}