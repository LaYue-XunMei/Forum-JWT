<script setup>

import LightCard from "@/components/LightCard.vue";
import Card from "@/components/Card.vue";
import Weather from "@/components/Weather.vue";
import {
  Avatar,
  Calendar,
  Clock,
  CollectionTag,
  Compass,
  Document,
  Edit,
  EditPen,
  Link, Microphone,
  Picture
} from "@element-plus/icons-vue";
import {computed, reactive, ref, watch} from "vue";
import {ElMessage} from "element-plus";

import {get,post} from "@/net/index.js";
import TopicEditor from "@/components/TopicEditor.vue";
import {userStore} from "@/store";
import axios from "axios";
import ColorDot from "@/components/ColorDot.vue";

const store = userStore();

const today = computed(()=>{
  const date = new Date()
  return `${date.getFullYear()}年${date.getMonth()+1}月${date.getDate()}日`
})

const weather =reactive({
  location: {},
  now: {},
  hourly:[],
  success :false //获取天气是否成功
})

const editor =ref(false)
const topics = reactive({
  list:[],
  type:0,
  page:0,
  end : false,
  // 无限滚动请求锁，避免同一页在接口返回前被重复加载
  loading: false,
  top: []
})

// 用于丢弃切换分类后才返回的旧请求，防止旧数据混入当前列表
let requestId = 0

watch(() => topics.type,()=>{
  resetList()
},{immediate:true})


get('/api/forum/types',data =>{
  const array = []
  array.push({name: '全部', id: 0,color:'linear-gradient(45deg,red,orange,gold,green,blue'})
  data.forEach(item=>{array.push(item)})
  store.forum.types = array
})
get('/api/forum/top-topic',data =>topics.top = data)

// 切换分类或发帖成功后，从第一页重新加载当前分类
function resetList(){
  requestId++
  topics.page = 0
  topics.list = []
  topics.end = false
  topics.loading = false
  updateList()
}

function updateList(){
  if(topics.end || topics.loading) return
  topics.loading = true
  const currentRequest = ++requestId
  get(`api/forum/list-topic?page=${topics.page}&type=${topics.type}`, data => {
    // 如果请求期间切换了分类，只保留最新一次列表请求的结果
    if(currentRequest !== requestId) return
    if(data){
      data.forEach(item=> topics.list.push(item))
      topics.page++
    }
    if(!data || data.length < 10)
      topics.end = true
    topics.loading = false
  }, () => {
    if(currentRequest === requestId)
      topics.loading = false
  })
}

function onTopicCreate(){
  editor.value = false

}

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
        <div style="margin-top:10px;display: flex;gap:13px;font-size:18px;color:grey">
          <el-icon><Edit/></el-icon>
          <el-icon><Document/></el-icon>
          <el-icon><Compass/></el-icon>
          <el-icon><Picture/></el-icon>
          <el-icon><Microphone/></el-icon>
        </div>
      </light-card>
      <light-card style="margin-top:10px;display: flex;flex-direction: column;gap:10px">
        <div v-for="item in topics.top" class="top-topic">
          <el-tag type="info" size="small">置顶</el-tag>
          <div>{{item.title}}</div>
          <div>{{new Date(item.time).toLocaleDateString()}}</div>

        </div>

      </light-card>
      <light-card style="margin-top: 10px;display: flex;gap: 7px;">
        <div :class="`type-select-card ${topics.type === item.id ? 'active':''}`"
             v-for="item in store.forum.types"
             @click="topics.type = item.id">
          <color-dot :color="item.color"/>
          <span style="margin-left: 5px">{{item.name}}</span>
        </div>
      </light-card>
      <transition name="el-fade-in" mode="out-in">
        <div v-if="topics.list.length">
          <!-- loading 或到底时禁用无限滚动，避免重复触发 updateList -->
          <div style="margin-top: 10px;display: flex;flex-direction: column;gap: 10px"
                v-infinite-scroll="updateList"
                :infinite-scroll-disabled="topics.loading || topics.end">
            <light-card  v-for="item in topics.list" :key="item.id" class="topic-card">
              <div style="display:flex">
                <div>
                  <el-avatar :size="30" :src="`${axios.defaults.baseURL}/images${item.avatar}`"/>
                </div>
                <div style="margin-left: 7px;transform: translateY(-2px)">
                  <div style="font-size: 13px;font-weight: bold">{{item.username}}</div>
                  <div style="font-size: 12px;color: gray;transform: translateY(-2px)">
                    <el-icon><Clock/></el-icon>
                    <div style="margin-left: 2px;display: inline-block">
                      {{new Date(item.time).toLocaleString()}}
                    </div>
                  </div>
                </div>
              </div>
              <div>
                <div class="topic-type"
                     :style="{
                    color:store.findTypeById(item.type)?.color + 'EE',
                    'border-color':store.findTypeById(item.type)?.color + '77',
                    'background-color':store.findTypeById(item.type)?.color + '22',
                 }">
                  {{store.findTypeById(item.type)?.name}}
                </div>
                <span style="font-weight: bold;margin: 7px">{{item.title}}</span>
              </div>
              <div class="topic-content">
                {{item.text}}
              </div>
              <div style="display:grid;grid-template-columns:repeat(3,1fr);grid-gap:10px">
                <el-image class="topic-image" v-for="img in item.images" :src="img" fit="cover"></el-image>
              </div>
            </light-card>
          </div>
        </div>

      </transition>

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

    <topic-editor :show="editor" @success="editor=false;resetList()" @close="editor=false"/>

  </div>
</template>

<style lang="less" scoped>

.top-topic{
  display: flex;
  div:first-of-type{
    font-size:14px;
    margin-left: 10px;
    font-weight: bold;
    opacity: 0.8;
    transition: color .3s;

    &:hover{
      color:gray;
    }
  }

  div:nth-of-type(2){
    flex:1;
    color:gray;
    font-size:13px;
    text-align: right;
  }

  &:hover{
    cursor:pointer;
  }
}

.type-select-card{
  background-color: #f5f5f5;
  padding: 2px 7px;
  font-size: 14px;
  border-radius: 5px;
  box-sizing:border-box;
  transition: background-color .3s;

  &.active{
    border:solid 1px #ccc9c9;
  }

  &:hover{
    cursor:pointer;
    background: #ada9a9;
  }
}

.topic-card{
  padding: 15px;
  transition: scale .3s;

  &:hover{
    scale:1.015;
    cursor: pointer;
  }
  .topic-content{
    font-size: 14px;
    color: grey;
    margin: 5px 0;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 3;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .topic-type{
    display: inline-block;
    border: solid 0.5px grey;
    border-radius: 5px;
    font-size: 12px;
    padding: 0 5px;
  }

  .topic-image{
    width: 100%;
    height: 100%;
    max-height: 110px;
    border-radius: 5px;
  }
}

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

.el-icon{
  translate:0 2px
}

.dark {
  .create-topic{
    background-color: #232323;
  }

  .type-select-card{
    background-color: #282828;

    &.active{
      border:solid 1px #64594b;
    }

    &:hover{
      background-color: #5e5e5e;
    }

  }
}
</style>
