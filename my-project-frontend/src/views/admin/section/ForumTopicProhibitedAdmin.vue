<script setup>
import {aoiForumProhibitedList, apiForumProhibitedSave} from "@/net/api/forum";
import {ref} from "vue";
import {ElMessage} from "element-plus";



const prohibitedWords = ref('')

const saveProhibitedWord = () =>{
    const list = prohibitedWords.value.split(',');
    apiForumProhibitedSave(list,()=> ElMessage.success("违禁词更新成功"))

}

aoiForumProhibitedList(data => prohibitedWords.value = data.join(","))
</script>

<template>
    <div class="prohibited-input">
        <div class="title">违禁词管理</div>
        <div class="desc">所有包含违禁词的帖子与评论都被限制发布，使用逗号隔开</div>
        <el-input type="textarea" :rows="8" v-model="prohibitedWords"/>
        <div style="text-align:right;margin-top: 20px">
            <el-button type="primary" @click="saveProhibitedWord">保持违禁词列表</el-button>
        </div>

    </div>
</template>

<style lang="less" scoped>

.prohibited-input{
    margin-top: 40px;
}

.title{
    font-weight: bold;
}
.desc{
    color:#bababa;
    font-size:13px;
    margin-left:20px;
}

</style>