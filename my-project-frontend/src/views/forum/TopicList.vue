<script setup>

import LightCard from "@/components/LightCard.vue";
import Card from "@/components/Card.vue";
import Weather from "@/components/Weather.vue";
import {Calendar, CollectionTag, EditPen, Link} from "@element-plus/icons-vue";
import {computed, reactive, ref} from "vue";
import {ElMessage} from "element-plus";

import {get,post} from "@/net/index.js";
import TopicEditor from "@/components/TopicEditor.vue";

const today = computed(()=>{
  const date = new Date()
  return `${date.getFullYear()}年${date.getMonth()+1}月${date.getDate()}日`
})

const editor =ref(false)

const weather =reactive({
  location: {},
  now: {},
  hourly:[],
  success :false //获取天气是否成功
})

const list = ref(null)
// get('api/forum/list-topic?page=1&type=0',data => list.value = data)

navigator.geolocation.getCurrentPosition(position =>{
      const longitude = position.coords.longitude
      const latitude = position.coords.latitude
      console.info(latitude,longitude)
      get(`/api/forum/weather?longitude=${longitude}&latitude=${latitude}`,data=>{
        Object.assign(weather,data)
        weather.success = true
      })
    }, error => {
      console.error('获取位置失败:', error.code, error.message);

      switch (error.code) {
        case error.PERMISSION_DENIED:
          ElMessage.warning("位置权限被拒绝，请允许网站获取位置信息");
          break;
        case error.POSITION_UNAVAILABLE:
          ElMessage.warning("位置信息不可用");
          break;
        case error.TIMEOUT:
          ElMessage.warning("获取位置超时");
          break;
        default:
          ElMessage.warning("无法获取位置信息");
      }

      // 使用默认位置（北京）
      get(`/api/forum/weather?longitude=117.02191&latitude=32.553011`, data => {
        Object.assign(weather, data);
        weather.success = true;
      });
    },
    {
      timeout: 10000,  // 延长到10秒
      maximumAge: 600000,  // 缓存10分钟
      enableHighAccuracy: false  // 设为false可能更快
    }
)

</script>

<template>
  <div style="display: flex;margin: 20px auto;gap: 20px;max-width: 1000px">
    <div style="flex: 1">
      <light-card>
        <div class="create-topic" @click="editor=true">
          <el-icon><EditPen/></el-icon>
          点击发表主题贴
        </div>
      </light-card>
      <light-card style="margin-top: 10px;height: 30px">

      </light-card>
      <div style="margin-top: 10px;display: flex;flex-direction: column;gap: 10px">
        <light-card style="height: 150px" v-for="item in 10">

        </light-card>
      </div>
    </div>

    <div style="width: 300px">
      <div style="position: sticky;top: 20px">
        <light-card>
          <div style="font-weight: bold ">
            <el-icon ><CollectionTag/></el-icon>
            论坛公告
          </div>
          <el-divider style="margin: 10px 0"/>
          <div style="font-size: 14px;margin:10px;color: grey">
            公告内容公告内容公告内容公告内容
            公告内容公告内容公告内容公告内容
          </div>
        </light-card>

        <light-card >
          <div style="font-weight: bold ">
            <el-icon><Calendar/></el-icon>
            天气信息
          </div>
          <el-divider style="margin: 10px 0"/>
          <weather :data="weather"/>
        </light-card>

        <light-card style="margin: 10px">
          <div class="info-text">
            <div>当前日期</div>
            <div>{{today}}</div>
          </div>
          <div class="info-text">
            <div>IP地址</div>
            <div>127.0.0.1</div>
          </div>
        </light-card>

        <div style="font-size: 14px;margin: 10px;color: grey">
          <el-icon><Link/></el-icon>
          友情链接
          <el-divider style="margin: 10px 0"/>
        </div>
        <div style="display: grid;grid-template-columns: repeat(2,1fr);grid-gap: 10px;margin-top: 10px">
          <div class="friend-link">
            <el-image style="height: 100%" src="https://element-plus.org/images/vform-banner.png"/>
          </div>
          <div class="friend-link">
            <el-image style="height: 100%" src="https://element-plus.org/images/vform-banner.png"/>
          </div>
          <div class="friend-link">
            <el-image style="height: 100%" src="https://element-plus.org/images/vform-banner.png"/>
          </div>
        </div>


      </div>
    </div>

    <topic-editor :show="editor" @success="editor=false" @close="editor=false"/>

  </div>
</template>

<style lang="less" scoped>
.create-topic{
  background-color: #efefef;
  border-radius: 5px;
  height: 40px;
  font-size: 14px;
  line-height: 40px;
  padding: 0 10px;
  color: grey;
  &:hover{
    cursor: pointer;
  }
}

.info-text{
  display: flex;
  justify-content: space-between;
  color: grey;
  font-size: 14px
}

.friend-link{
  border-radius: 5px;
  overflow: hidden;
}

.dark .create-topic{
  background-color: #494949;
}

.el-icon{
 translate:0 2px
}
</style>