<script setup>
import {apiForumUserTopic, apiForumUserTopicDelete} from "@/net/api/user";
import {ref} from "vue";
import Card from "@/components/Card.vue";
import {apiForumTypes} from "@/net/api/forum";
import {useStore} from "@/store";
import TopicTag from "@/components/TopicTag.vue";
import {Clock, Delete, Hide, Lock} from "@element-plus/icons-vue";
import {ElMessage, ElMessageBox, valueEquals} from "element-plus";

const store = useStore()

const list = ref([])

const deleteTopic = (tid) =>{
    ElMessageBox.confirm("删除后帖子将无法找回，是否删除","删除帖子",{
        callback: value =>{
            if(value === "confirm"){
                apiForumUserTopicDelete(tid,() => {
                    ElMessage.success("帖子删除成功")
                    refreshList()
                })
            }
        }
    })

}


const refreshList = () =>  apiForumUserTopic(data => list.value = data)

refreshList()
apiForumTypes(data => store.forum.types = data)
</script>

<template>
    <div style="margin:auto;max-width: 700px">
        <div class="topic-list">
            <card v-for=" topic in list">
                <div class="title">
                    <el-tag size="small" effect="dark" type="info" style="margin-right:10px"
                            v-if="topic.invisible" disable-transitions>
                        <el-icon><Hide/></el-icon>
                        屏蔽
                    </el-tag>
                    <el-tag size="small" effect="dark" type="warning" style="margin-right:10px"
                    v-if="topic.locked" disable-transitions>
                        <el-icon><Lock/></el-icon>
                        已锁定
                    </el-tag>
                    <topic-tag style="margin-right: 10px" :type="topic.type"/>
                    <el-link :href="`/index/topic-detail/${topic.id}`">{{ topic.title }}</el-link>
                </div>
                <div class="content">
                    <div style="font-size: 12px;color: gray">
                        <el-icon ><Clock/></el-icon>
                        <div style="margin-left: 2px;display: inline-block">
                            {{ new Date(topic.time).toLocaleString()}}
                        </div>
                    </div>
                    <el-link type="danger" @click="deleteTopic(topic.id)">
                        <el-icon><Delete/></el-icon>&nbsp;
                        <span>删除帖子</span>
                    </el-link>

                </div>
            </card>

        </div>

    </div>

</template>

<style lang="less" scoped>
.topic-list{
    gap:10px;
    display: flex;
    flex-direction: column;
    margin: 20px 0;

    .content{
        display: flex;
        justify-content: space-between;
        margin-top: 10px;

    }
}
</style>