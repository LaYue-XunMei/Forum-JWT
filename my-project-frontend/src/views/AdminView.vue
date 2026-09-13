<script setup>

import {
  Bell,
  ChatDotSquare,
  Collection,
  DataLine,
  Document,
  Files,
  Location, Lock,
  Monitor, Notification, Operation,
  Position, School,
  Umbrella, User
} from "@element-plus/icons-vue";
import UserInfo from "@/components/UserInfo.vue";
import {get} from "@/net";
import {useStore} from "@/store";
import {inject, onMounted, ref} from "vue";
import router from "@/router";
import {useRoute} from "vue-router";

const adminMenu = [
  {
    title:'校园论坛管理',icon:Location,sub:[
      {title:'用户管理',icon:User,index:'/admin/user'},
      {title:'帖子广场管理',icon:ChatDotSquare,index:'/admin/forum'},
      {title:'失物招领管理',icon:Bell},
      {title:'校园活动管理',icon:Notification},
      {title:'表白墙管理',icon:Umbrella},
      {title:'合作机构管理',icon:School},
    ]
  }, {
    title:'探索与发现管理',icon:Location,sub:[
      {title:'成绩管理',icon:Document},
      {title:'课程表管理',icon:Files},
      {title:'教务通知管理',icon:Monitor},
      {title:'在线图书管理',icon:Collection},
      {title:'预约教室管理',icon:DataLine},
    ]
  }
]

const route = useRoute()
const loading = inject('userLoading')

const pageTabs = ref([])

function handleTabClick({props}){
  router.push(props.name)//name 就是路径 ,之前的设置的index
}

function handleTabClose(name){//这里的name就是之前给的路径
  const index = pageTabs.value.findIndex(tab => tab.name === name)//先找到当前页面的index，这里是数字
  const isCurrent = name === route.fullPath
  pageTabs.value.splice(index,1)
  if(pageTabs.value.length > 0){
    //删除后，并且关闭的是当前页面，标签列表中还有剩余，自动切换上一个，否则切换下一个
    //简写：router.push(pageTabs.value[Math.max(0,index-1)].name)
    if(isCurrent){
      if(index === 0){//删除的是最左边第一个页面，并且是当前页面
        router.push(pageTabs.value[0].name)//自动换成删除后的新第一个页面
      }else{//不是第一个页面，则换成前一个
        router.push(pageTabs.value[index - 1].name)
      }
    }
  }else{
    router.push('/admin')
  }
}

function addAdminTab(menu){
  if(!menu.index) return
  if(pageTabs.value.findIndex(tab => tab.name === menu.index) < 0){//没有重复的才添加
    pageTabs.value.push({
      title: menu.title,
      name: menu.index
    })
  }
}
onMounted(()=>{
  const initPage = adminMenu
      .flatMap(menu => menu.sub)
      .find(sub => sub.index === route.fullPath)//检查是否直接访问了可以作为标签页的页面
  if(initPage){
    addAdminTab(initPage)
  }
})
</script>

<template>
  <div class="admin-content" v-loading="loading" element-loading-text="正在进入，请稍后...">
      <el-container style="height: 100%">
        <el-aside width="230px" class="admin-content-aside">
          <div class="logo-box">
            <div style="width:320px;height:32px">
              <el-image class="logo" src="https://element-plus.org/images/element-plus-logo.svg"></el-image>
            </div>

          </div>
          <el-scrollbar style="height: calc(100vh - 57px);">
            <el-menu
                router
                :default-active="$route.path"
                :default-openeds="['1','2']"
                style="min-height: calc(100vh - 57px);border: none">
              <el-sub-menu :index="(index+1).toString()"
                           v-for="(menu,index) in adminMenu">
                <template #title>
                  <el-icon><component :is="menu.icon"/></el-icon>
                  <span><b>{{menu.title}}</b></span>
                </template>
                <el-menu-item :index="subMenu.index"
                              @click="addAdminTab(subMenu)"
                              v-for="subMenu in menu.sub">
                  <template #title>
                    <el-icon><component :is="subMenu.icon"/></el-icon>
                    {{subMenu.title}}
                  </template>
                </el-menu-item>
              </el-sub-menu>
            </el-menu>
          </el-scrollbar>


        </el-aside>
        <el-container>
          <el-header class="admin-content-header">
            <div style="flex: 1">
              <el-tabs type="card"
                       :model-value="route.fullPath"
                       closable
                       @tab-remove="handleTabClose"
                       @tab-click="handleTabClick">
                <el-tab-pane v-for=" tab in pageTabs"
                             :label="tab.title"
                             :name="tab.name"
                             :key="tab.name"/>
              </el-tabs>
            </div>
            <user-info/>
          </el-header>
          <el-main>
            <router-view v-slot="{Component}">
              <keep-alive>
                <component :is="Component"/>
              </keep-alive>
            </router-view>
          </el-main>
        </el-container>
      </el-container>
  </div>
</template>

<style scoped>

.admin-content{
  height: 100vh;
  width: 100vw;

  .admin-content-aside{
    border-right: solid 1px var(--el-border-color);

    .logo-box{
      text-align: center;
      padding: 15px 0 10px;
      height: 32px;
      .logo{
        height: 32px;
      }
    }
  }
  .admin-content-header{
    border-bottom: solid 1px var(--el-border-color);
    height: 55px;
    display: flex;
    align-items: center;
    box-sizing: border-box;

    :deep(.el-tabs__header){
      height: 32px;
      margin-bottom: 0;
      border-bottom:none;
    }
    :deep(.el-tabs__nav){
      gap:10px;
      border:none;
      margin-top:5px;
    }
    :deep(.el-tabs__item){
      height:32px;
      padding: 0 15px;
      border-radius: 6px;
      border:solid 1px var(--el-border-color);
    }
  }
}

</style>