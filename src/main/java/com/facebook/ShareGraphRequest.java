package com.facebook;

import android.net.Uri;
import android.os.Bundle;
import com.facebook.share.internal.OpenGraphJSONUtility;
import com.facebook.share.internal.OpenGraphJSONUtility.PhotoJSONProcessor;
import com.facebook.share.model.ShareOpenGraphObject;
import com.facebook.share.model.SharePhoto;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;

public class ShareGraphRequest {
    public static GraphRequest createOpenGraphObject(ShareOpenGraphObject shareOpenGraphObject) throws FacebookException {
        String string = shareOpenGraphObject.getString("type");
        if (string == null) {
            string = shareOpenGraphObject.getString("og:type");
        }
        if (string != null) {
            try {
                JSONObject jSONObject = (JSONObject) OpenGraphJSONUtility.toJSONValue(shareOpenGraphObject, new PhotoJSONProcessor() {
                    public JSONObject toJSONObject(SharePhoto sharePhoto) {
                        Uri imageUrl = sharePhoto.getImageUrl();
                        JSONObject jSONObject = new JSONObject();
                        try {
                            jSONObject.put("url", imageUrl.toString());
                            return jSONObject;
                        } catch (Exception e) {
                            throw new FacebookException("Unable to attach images", e);
                        }
                    }
                });
                Bundle bundle = new Bundle();
                bundle.putString("object", jSONObject.toString());
                Object[] objArr = new Object[2];
                objArr[0] = "me";
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("objects/");
                stringBuilder.append(string);
                objArr[1] = stringBuilder.toString();
                return new GraphRequest(AccessToken.getCurrentAccessToken(), String.format(Locale.ROOT, "%s/%s", objArr), bundle, HttpMethod.POST);
            } catch (JSONException e) {
                throw new FacebookException(e.getMessage());
            }
        }
        throw new FacebookException("Open graph object type cannot be null");
    }
}
