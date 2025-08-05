package com.example.talent_link.util

object ApiUrl {
    const val BASE_URL = "http://10.0.2.2:8099"

    object TalentSell {
        const val UPLOAD = "/api/talentsell"
    }

    object Auth {
        const val SIGNUP = "/api/auth/signup"
        const val LOGIN = "/api/auth/login"
    }
}
