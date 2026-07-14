<script setup>

import {get} from "@/net";
import {userStore} from "@/store";


const store = userStore();

get('/api/forum/types',data =>{
  const array = []
  array.push({name: '全部', id: 0,color:'linear-gradient(45deg,red,orange,gold,green,blue'})
  data.forEach(item=>{array.push(item)})
  store.forum.types = array
})

</script>

<template>


  <router-view v-slot="{ Component }">
    <transition name="el-fade-in-linear" mode="out-in">
      <keep-alive include="TopicList">
        <component :is="Component" style="height: 100%"></component>
      </keep-alive>
    </transition>
  </router-view>
</template>

<style scoped>

</style>