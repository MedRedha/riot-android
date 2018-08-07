/*
 * Copyright 2018 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.webview

import android.text.TextUtils
import im.vector.Matrix
import im.vector.activity.RiotAppCompatActivity
import im.vector.util.weak
import org.matrix.androidsdk.rest.callback.ApiCallback
import org.matrix.androidsdk.rest.model.MatrixError
import org.matrix.androidsdk.util.Log

private const val SUCCESS_URL = "https://matrix.org/_matrix/consent"
private const val RIOT_BOT_ID = "@riot-bot:matrix.org"
private const val LOG_TAG = "ConsentWebViewEventListener"

/**
 * This class is the Consent implementation of WebViewEventListener.
 * It is used to manage the consent agreement flow.
 */
class ConsentWebViewEventListener(activity: RiotAppCompatActivity, private val delegate: WebViewEventListener)
    : WebViewEventListener by delegate {

    private val safeActivity: RiotAppCompatActivity? by weak(activity)

    override fun onPageFinished(url: String) {
        delegate.onPageFinished(url)
        if (TextUtils.equals(url, SUCCESS_URL)) {
            createRiotBotRoom()
        }
    }

    /**
     * This methods try to create the RiotBot room when the user agreed
     */
    private fun createRiotBotRoom() {
        safeActivity?.let {
            it.showWaitingView()
            Matrix.getInstance(it).defaultSession.createDirectMessageRoom(RIOT_BOT_ID, createRiotBotRoomCallback)
        }
    }

    /**
     * APICallback instance
     */
    private val createRiotBotRoomCallback = object : ApiCallback<String> {
        override fun onSuccess(info: String) {
            Log.d(LOG_TAG, "## On success : succeed to invite riot-bot")
            safeActivity?.finish()
        }

        private fun onError(error: String?) {
            Log.e(LOG_TAG, "## On error : failed  to invite riot-bot $error")
            safeActivity?.finish()
        }

        override fun onNetworkError(e: Exception) {
            onError(e.message)
        }

        override fun onMatrixError(e: MatrixError) {
            onError(e.message)
        }

        override fun onUnexpectedError(e: Exception) {
            onError(e.message)
        }
    }

}