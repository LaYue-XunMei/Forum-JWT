package com.example.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.Account;
import com.example.entity.dto.StoreImage;
import com.example.mapper.AccountMapper;
import com.example.mapper.ImageStoreMapper;
import com.example.service.ImageService;
import com.example.utils.Const;
import com.example.utils.FlowUtils;
import io.minio.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
public class ImageServiceImpl extends ServiceImpl<ImageStoreMapper, StoreImage> implements ImageService {

    @Resource
    MinioClient minioClient;

    @Resource
    AccountMapper accountMapper;

    @Resource
    FlowUtils flowUtils;

    SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

    @Override
    public void fetchImageFromMinio(OutputStream stream, String image) throws Exception {
        GetObjectArgs args = GetObjectArgs.builder()
                .bucket("study")
                .object(image)
                .build();
        GetObjectResponse response = minioClient.getObject(args);
        IOUtils.copy(response,stream);//response 就是输入流，拷贝过去
    }

    @Override
    public String uploadImage(MultipartFile file, int id) throws IOException {
        String key = Const.FORUM_IMAGE_COUNTER+id;
        if(!flowUtils.limitPeriodCounterCheck(key,20,3600))//不通过返回空
            return null;
        String imageName = UUID.randomUUID().toString().replace("-","");
        Date date = new Date();
        imageName = "/cache/"+format.format(date) + "/" +imageName;
        PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                .bucket("study")
                .stream(file.getInputStream(),file.getSize(),-1)//-1 自动分配计算
                .object(imageName)
                .build();
        try{
            minioClient.putObject(putObjectArgs);
            if(this.save(new StoreImage(id,imageName,date))){
                return imageName;
            }else{
                return null;
            }
        }catch (Exception e){
            log.error("图片上传出现问题：{}", e.getMessage(), e);
            return null;
        }
    }


    @Override
    public String upLoadAvatar(MultipartFile file, int id) throws IOException {
        String imageName = UUID.randomUUID().toString().replace("-","");
        imageName = "/avatar/"+imageName;
        PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                .bucket("study")
                .stream(file.getInputStream(),file.getSize(),-1)//-1 自动分配计算
                .object(imageName)
                .build();
        try{
            minioClient.putObject(putObjectArgs);
            //先删除原头像
            String avatar = accountMapper.selectById(id).getAvatar();
            this.deleteOldAvatar(avatar);
            if(accountMapper.update(null, Wrappers.<Account>update().eq("id",id).set("avatar",imageName)) > 0){
                return imageName;
            }else {
                return null;
            }
        }catch (Exception e){
            log.error("图片上传出现问题：{}", e.getMessage(), e);
            return null;
        }
    }

    private void deleteOldAvatar(String avatar) throws Exception {//avatar数据库中查出的图像地址
        if(avatar == null || avatar.isEmpty()) return;
        RemoveObjectArgs remove = RemoveObjectArgs.builder()
                .bucket("study")
                .object(avatar)
                .build();
        minioClient.removeObject(remove);
    }


}
