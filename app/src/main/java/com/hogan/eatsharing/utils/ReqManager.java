package com.hogan.eatsharing.utils;

import android.util.Log;
import com.google.gson.Gson;
import com.hogan.eatsharing.config.CommentList;
import com.hogan.eatsharing.config.FoodList;
import com.hogan.eatsharing.config.LoginClass;
import com.hogan.eatsharing.config.RecipeList;
import com.hogan.eatsharing.config.UserList;
import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

public class ReqManager {

    private static ReqManager instance;

    private ReqManager(){ }

    public static ReqManager getInstance(){
        if (instance == null) {
            instance = new ReqManager();
        }
        return instance;
    }

    private static final String mHost = "http://192.168.137.1:8080/EatSharing/";
    private static final String mLogin = "login";
    private static final String mSign = "addUser";
    private static final String mHeadUpload = "headUpload";
    private static final String mRecipeRelease = "recipeRelease";
    private static final String mFoodRelease = "foodRelease";
    private static final String mRecipeCommon = "recipeRecomm";
    private static final String mFoodCommon = "foodRecomm";
    private static final String mMyRecipe = "myRecipe";
    private static final String mMyFood = "myFood";

    private static final String mFoodFocus = "foodFocus";
    private static final String mCheckCollect = "checkCollect";
    private static final String mSetCollect = "setCollect";
    private static final String mCheckFollow = "checkFollow";
    private static final String mSetFollow = "setFollow";
    private static final String mFollowCount = "followCount";
    private static final String mCollectRecipe = "collectRecipe";
    private static final String mCollectFood = "collectFood";
    private static final String mGetComments = "getComment";
    private static final String mSearchRecipe = "searchRecipe";
    private static final String mDeleteRecipe = "deleteRecipe";
    private static final String mDeleteFood = "deleteFood";
    private static final String mUpdateMsg = "updateUserMsg";
    private static final String mAddComment = "addComment";
    private static final String mFocusUser = "focusUser";
    private static final String mUpdateRecipe = "recipeUpdate";
    private static final String mUpdateFood = "foodUpdate";


    public String getHost(){return mHost;}

    private Gson gson = new Gson();
    private OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(8,TimeUnit.SECONDS)
            .build();

    private static String responseBody(Response response) {

        Charset UTF8 = StandardCharsets.UTF_8;
        ResponseBody responseBody = response.body();
        assert responseBody != null;
        BufferedSource source = responseBody.source();
        try {
            source.request(Long.MAX_VALUE); // Buffer the entire body.
        } catch (IOException e) {
            e.printStackTrace();
        }
        Buffer buffer = source.buffer();

        Charset charset = UTF8;
        MediaType contentType = responseBody.contentType();
        if (contentType != null) {
            try {
                charset = contentType.charset(UTF8);
            } catch (UnsupportedCharsetException e) {
                e.printStackTrace();
            }
        }
        assert charset != null;
        return buffer.clone().readString(charset);
    }

    public interface requestCallback{
        void callBack(boolean success,String message);
    }

    public interface loginCallback{
        void callBack(boolean success,LoginClass loginClass);
    }

    public interface recipeCallback{
        void callBack(boolean success,RecipeList recipeList);
    }

    public interface foodCallback{
        void callBack(boolean success, FoodList foodList);
    }

    public interface commentCallback{
        void callBack(boolean success, CommentList commentList);
    }

    public interface userCallback{
        void callBack(boolean success, UserList userList);
    }

    public void deleteRecipe(String rid,requestCallback callback){
        String url = mHost+mDeleteRecipe;

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("rid",rid);
        RequestBody requestBody = formBuilder.build();

        Request.Builder builder =new Request.Builder().url(url).post(requestBody);

        client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                callback.callBack(false,"fail");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                callback.callBack(true,responseBody(response));
            }
        });
    }

    public void deleteFood(String fid,requestCallback callback){
        String url = mHost+mDeleteFood;

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("fid",fid);
        RequestBody requestBody = formBuilder.build();

        Request.Builder builder =new Request.Builder().url(url).post(requestBody);

        client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                callback.callBack(false,"fail");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                callback.callBack(true,responseBody(response));
            }
        });
    }

    public void getFocusUser(String uid,userCallback callback){
        String url = mHost+mFocusUser;

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("uid_user",uid);
        RequestBody requestBody = formBuilder.build();

        Request.Builder builder =new Request.Builder().url(url).post(requestBody);

        client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                callback.callBack(false,null);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try{
                    String jsonStr = responseBody(response);
                    Log.d("user", "onResponse: "+jsonStr);
                    UserList userList = gson.fromJson(jsonStr,UserList.class);
                    callback.callBack(true,userList);
                }catch (Exception e){
                    e.printStackTrace();
                    callback.callBack(true,null);
                }
            }
        });
    }

    public void upLoadHead(File mFile,String username,requestCallback callback){
        String url = mHost+mHeadUpload;

        MediaType mediaType= MediaType.parse("application/octet-stream");
        // 设置文件以及文件上传类型封装
        RequestBody requestBody = RequestBody.create(mediaType, mFile);

        // 文件上传的请求体封装
        MultipartBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("username",username)
                .addFormDataPart("file", mFile.getName(), requestBody)
                .build();

        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(multipartBody);

        client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                callback.callBack(false,"fail");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response){
                callback.callBack(true,responseBody(response));
            }
        });
    }

    public void login(String username,String password,loginCallback callback){
        String url = mHost+mLogin;

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("username",username);
        formBuilder.add("password",password);

        RequestBody requestBody = formBuilder.build();

        Request.Builder builder =new Request.Builder().url(url).post(requestBody);

        client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                callback.callBack(false,null);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try{
                    String jsonStr = responseBody(response);
                    LoginClass loginClass = gson.fromJson(jsonStr,LoginClass.class);
                    callback.callBack(true,loginClass);
                }catch (Exception e){
                    e.printStackTrace();
                    callback.callBack(true,null);
                }
            }
        });
    }

    public void signUp(String username,String password,requestCallback callback){
        String url = mHost+mSign;

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("username",username);
        formBuilder.add("password",password);

        RequestBody requestBody = formBuilder.build();

        Request.Builder builder =new Request.Builder().url(url).post(requestBody);

        client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                callback.callBack(false,"fail");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                callback.callBack(true,responseBody(response));
            }
        });
    }

    public void checkCollect(String uid,String cid,requestCallback callback){
        String url = mHost+mCheckCollect;

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("uid",uid);
        formBuilder.add("collectId",cid);

        RequestBody requestBody = formBuilder.build();

        Request.Builder builder =new Request.Builder().url(url).post(requestBody);

        client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                callback.callBack(false,"fail");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Log.d("count", "onCount: "+responseBody(response));
                callback.callBack(true,responseBody(response));

            }
        });
    }

    public void followCount(String username,requestCallback callback){
        String url = mHost+mFollowCount;

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("username",username);

        RequestBody requestBody = formBuilder.build();

        Request.Builder builder =new Request.Builder().url(url).post(requestBody);

        client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                callback.callBack(false,"fail");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                callback.callBack(true,responseBody(response));
            }
        });
    }

    public void checkFollow(String uid,String uid_ed,requestCallback callback){
        String url = mHost+mCheckFollow;

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("uid",uid);
        formBuilder.add("uid_ed",uid_ed);

        RequestBody requestBody = formBuilder.build();

        Request.Builder builder =new Request.Builder().url(url).post(requestBody);

        client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                callback.callBack(false,"fail");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                callback.callBack(true,responseBody(response));
            }
        });
    }

    public void addComment(String username,String content,String sort,String main_id,requestCallback callback){
        String url = mHost+mAddComment;

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("username",username);
        formBuilder.add("sort",sort);
        formBuilder.add("content",content);
        formBuilder.add("main_id",main_id);

        RequestBody requestBody = formBuilder.build();

        Request.Builder builder =new Request.Builder().url(url).post(requestBody);

        client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                callback.callBack(false,"fail");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                callback.callBack(true,responseBody(response));
            }
        });
    }

    public void getComments(String main_id,commentCallback callback){
        String url = mHost+mGetComments;
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("main_id",main_id);

        RequestBody requestBody = formBuilder.build();

        Request.Builder builder =new Request.Builder().url(url).post(requestBody);

        client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                callback.callBack(false,null);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try{
                    String jsonStr = responseBody(response);
                    Log.d("123", "onResponse: "+jsonStr);
                    CommentList commentList = gson.fromJson(jsonStr,CommentList.class);
                    callback.callBack(true,commentList);
                }catch (Exception e){
                    e.printStackTrace();
                    callback.callBack(true,null);
                }
            }
        });
    }

    public void setCollect(String uid,String sort,String cid,requestCallback callback){
        String url = mHost+mSetCollect;

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("uid",uid);
        formBuilder.add("sort",sort);
        formBuilder.add("collectId",cid);

        RequestBody requestBody = formBuilder.build();

        Request.Builder builder =new Request.Builder().url(url).post(requestBody);

        client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                callback.callBack(false,"fail");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                callback.callBack(true,responseBody(response));
            }
        });
    }

    public void setFollow(String uid,String uid_ed,requestCallback callback){
        String url = mHost+mSetFollow;

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("uid",uid);
        formBuilder.add("uid_ed",uid_ed);

        RequestBody requestBody = formBuilder.build();

        Request.Builder builder =new Request.Builder().url(url).post(requestBody);

        client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                callback.callBack(false,"fail");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                callback.callBack(true,responseBody(response));
            }
        });
    }

    public void recipeRelease(File coverFile, String title, String ingre, String step, List<File> images, String tip, String sort, String username, requestCallback callback){
        String url = mHost + mRecipeRelease;

        MediaType mediaType= MediaType.parse("application/octet-stream");
        // 设置文件以及文件上传类型封装
        RequestBody requestBody1 = RequestBody.create(mediaType, coverFile);

        // 文件上传的请求体封装
        MultipartBody.Builder mBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        mBody.addFormDataPart("coverFile",coverFile.getName(),requestBody1);
        mBody.addFormDataPart("title", title);
        mBody.addFormDataPart("username",username);
        mBody.addFormDataPart("ingre",ingre);
        mBody.addFormDataPart("step",step);
        mBody.addFormDataPart("tip",tip);
        mBody.addFormDataPart("sort",sort);
        for(File file:images){
            if(file.exists()){
                mBody.addFormDataPart("files",file.getName(),RequestBody.create(mediaType,file));
            }
        }

        RequestBody multipartBody = mBody.build();
        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(multipartBody);

        client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                Log.d("fail", "onFailure: "+"???");
                callback.callBack(false,"fail");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response){
                callback.callBack(true,responseBody(response));
            }
        });
    }

    public void updateRecipe(File coverFile, String title, String ingre, String step, List<File> images, String tip, String sort, String rid, requestCallback callback){
        String url = mHost + mUpdateRecipe;

        MediaType mediaType= MediaType.parse("application/octet-stream");
        // 设置文件以及文件上传类型封装
        RequestBody requestBody1 = RequestBody.create(mediaType, coverFile);

        // 文件上传的请求体封装
        MultipartBody.Builder mBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        mBody.addFormDataPart("coverFile",coverFile.getName(),requestBody1);
        mBody.addFormDataPart("title", title);
        mBody.addFormDataPart("rid",rid);
        mBody.addFormDataPart("ingre",ingre);
        mBody.addFormDataPart("step",step);
        mBody.addFormDataPart("tip",tip);
        mBody.addFormDataPart("sort",sort);
        for(File file:images){
            if(file.exists()){
                mBody.addFormDataPart("files",file.getName(),RequestBody.create(mediaType,file));
            }
        }

        RequestBody multipartBody = mBody.build();
        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(multipartBody);

        client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                callback.callBack(false,"fail");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response){
                callback.callBack(true,responseBody(response));
            }
        });
    }

    public void foodRelease(File file,String title,String content,String tag,String username,requestCallback callback){
        String url = mHost+mFoodRelease;

        MediaType mediaType= MediaType.parse("application/octet-stream");
        // 设置文件以及文件上传类型封装
        RequestBody requestBody = RequestBody.create(mediaType, file);

        // 文件上传的请求体封装
        MultipartBody.Builder mBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        mBody.addFormDataPart("file", file.getName(), requestBody);
        mBody.addFormDataPart("title",title);
        mBody.addFormDataPart("content",content);
        mBody.addFormDataPart("username",username);
        Log.d("username", "foodRelease: "+username);
        mBody.addFormDataPart("tag",tag);

        RequestBody multipartBody = mBody.build();
        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(multipartBody);

        client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                callback.callBack(false,"fail");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response){
                callback.callBack(true,responseBody(response));
            }
        });
    }

    public void updateFood(File file,String title,String content,String tag,String fid,requestCallback callback){
        String url = mHost+mUpdateFood;

        MediaType mediaType= MediaType.parse("application/octet-stream");
        // 设置文件以及文件上传类型封装
        RequestBody requestBody = RequestBody.create(mediaType, file);

        // 文件上传的请求体封装
        MultipartBody.Builder mBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        mBody.addFormDataPart("file", file.getName(), requestBody);
        mBody.addFormDataPart("title",title);
        mBody.addFormDataPart("content",content);
        mBody.addFormDataPart("fid",fid);
        mBody.addFormDataPart("tag",tag);

        RequestBody multipartBody = mBody.build();
        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(multipartBody);

        client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                callback.callBack(false,"fail");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response){
                callback.callBack(true,responseBody(response));
            }
        });
    }

    public void getRecipes(recipeCallback callback){
        String url = mHost+mRecipeCommon;

        FormBody.Builder formBuilder = new FormBody.Builder();

        RequestBody requestBody = formBuilder.build();

        Request.Builder builder =new Request.Builder().url(url).post(requestBody);

        client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                callback.callBack(false,null);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try{
                    String jsonStr = responseBody(response);
                    Log.d("123", "onResponse: "+jsonStr);
                    RecipeList recipeList = gson.fromJson(jsonStr,RecipeList.class);
                    callback.callBack(true,recipeList);
                }catch (Exception e){
                    e.printStackTrace();
                    callback.callBack(true,null);
                }
            }
        });
    }

    public void getFoods(foodCallback callback){
        String url = mHost+mFoodCommon;

        FormBody.Builder formBuilder = new FormBody.Builder();

        RequestBody requestBody = formBuilder.build();

        Request.Builder builder =new Request.Builder().url(url).post(requestBody);

        client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                callback.callBack(false,null);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try{
                    String jsonStr = responseBody(response);
                    Log.d("123", "onResponse: "+jsonStr);
                    FoodList foodList = gson.fromJson(jsonStr,FoodList.class);
                    callback.callBack(true,foodList);
                }catch (Exception e){
                    e.printStackTrace();
                    callback.callBack(true,null);
                }
            }
        });
    }

    public void getMyFoods(String username,foodCallback callback){
        String url = mHost+mMyFood;

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("username",username);

        RequestBody requestBody = formBuilder.build();

        Request.Builder builder =new Request.Builder().url(url).post(requestBody);

        client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                callback.callBack(false,null);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try{
                    String jsonStr = responseBody(response);
                    Log.d("123", "onResponse: "+jsonStr);
                    FoodList foodList = gson.fromJson(jsonStr,FoodList.class);
                    callback.callBack(true,foodList);
                }catch (Exception e){
                    e.printStackTrace();
                    callback.callBack(true,null);
                }
            }
        });
    }

    public void getCollectFood(String uid,foodCallback callback){
        String url = mHost+mCollectFood;

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("uid",uid);

        RequestBody requestBody = formBuilder.build();

        Request.Builder builder =new Request.Builder().url(url).post(requestBody);

        client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                callback.callBack(false,null);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try{
                    String jsonStr = responseBody(response);
                    Log.d("123", "onResponse: "+jsonStr);
                    FoodList foodList = gson.fromJson(jsonStr,FoodList.class);
                    callback.callBack(true,foodList);
                }catch (Exception e){
                    e.printStackTrace();
                    callback.callBack(true,null);
                }
            }
        });
    }

    public void getMyRecipes(String username,recipeCallback callback){
        String url = mHost+mMyRecipe;

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("username",username);

        RequestBody requestBody = formBuilder.build();

        Request.Builder builder =new Request.Builder().url(url).post(requestBody);

        client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                callback.callBack(false,null);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try{
                    String jsonStr = responseBody(response);
                    Log.d("123", "onResponse: "+jsonStr);
                    RecipeList recipeList = gson.fromJson(jsonStr,RecipeList.class);
                    callback.callBack(true,recipeList);
                }catch (Exception e){
                    e.printStackTrace();
                    callback.callBack(true,null);
                }
            }
        });
    }

    public void getCollectRecipe(String username,recipeCallback callback){
        String url = mHost + mCollectRecipe;

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("username",username);

        RequestBody requestBody = formBuilder.build();

        Request.Builder builder =new Request.Builder().url(url).post(requestBody);

        client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                callback.callBack(false,null);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try{
                    String jsonStr = responseBody(response);
                    Log.d("123", "onResponse: "+jsonStr);
                    RecipeList recipeList = gson.fromJson(jsonStr,RecipeList.class);
                    callback.callBack(true,recipeList);
                }catch (Exception e){
                    e.printStackTrace();
                    callback.callBack(true,null);
                }
            }
        });
    }

    public void searchRecipe(String key,recipeCallback callback){
        String url = mHost + mSearchRecipe;

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("key",key);

        RequestBody requestBody = formBuilder.build();

        Request.Builder builder =new Request.Builder().url(url).post(requestBody);

        client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                callback.callBack(false,null);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try{
                    String jsonStr = responseBody(response);
                    Log.d("123", "onResponse: "+jsonStr);
                    RecipeList recipeList = gson.fromJson(jsonStr,RecipeList.class);
                    callback.callBack(true,recipeList);
                }catch (Exception e){
                    e.printStackTrace();
                    callback.callBack(true,null);
                }
            }
        });
    }

    public void updateMsg(String username,String sex,String pro,String birthday,String city,String intro,requestCallback callback){
        String url = mHost+mUpdateMsg;

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("username",username);
        formBuilder.add("sex",sex);
        formBuilder.add("birthday",birthday);
        formBuilder.add("profession",pro);
        formBuilder.add("city",city);
        formBuilder.add("introduction",intro);

        RequestBody requestBody = formBuilder.build();

        Request.Builder builder =new Request.Builder().url(url).post(requestBody);

        client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                callback.callBack(false,"fail");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                callback.callBack(true,responseBody(response));
            }
        });
    }
}
