<script setup>
import {reactive, ref, watchEffect} from "vue";
import {
    aoiForumProhibitedList, apiForumProhibitedSave,
    apiForumTopicAllList,
    apiForumTopicDelete, apiForumTopicInvisible,
    apiForumTopicLocked,
    apiForumTopicTop,
    apiForumTypes
} from "@/net/api/forum";
import {User} from "@element-plus/icons-vue";
import {useStore} from "@/store";
import {ElMessage, ElMessageBox} from "element-plus";

const store = useStore()
const topicList = reactive({
    list:[],
    page:1,
    size:10,
    total:0
})
const types = ref([])
const findType = type => types.value.find(item => item.id === type)
const prohibitedWords = ref('')
const deleteTopic = id =>{
    ElMessageBox.confirm("确定要删除该帖子吗？删除后无法恢复，请确认",{
        callback: value =>{
            if(value === "confirm"){
                apiForumTopicDelete(id,()=>{
                    refreshList()
                    ElMessage.success("成功删除帖子")
                })
            }
        }
    })
}

const topTopic = (tid,status) =>{
    apiForumTopicTop({tid, status}, data =>{
        ElMessage.success("帖子置顶状态已修改")
        refreshList()
    })
}

const lockTopic = (tid,status) =>{
    apiForumTopicLocked({tid, status}, data =>{
        ElMessage.success("帖子锁定状态已修改")
        refreshList()
    })
}

const invisibleTopic = (tid,status) =>{
    apiForumTopicInvisible({tid, status}, data =>{
        ElMessage.success("帖子封禁状态已修改")
        refreshList()
    })
}

const saveProhibitedWord = () =>{
    const list = prohibitedWords.value.split(',');
    apiForumProhibitedSave(list,()=> ElMessage.success("违禁词更新成功"))

}

const refreshList =() => {
    apiForumTopicAllList(topicList.page,topicList.size,data =>{
    topicList.list = data.list;
    topicList.total = data.total;
})
}

watchEffect(() => refreshList())

apiForumTypes(data => types.value = data)
aoiForumProhibitedList(data => prohibitedWords.value = data.join(","))
</script>

<template>
  <div class="forum-admin">
      <div class="title">
          <el-icon><User/></el-icon>
          论坛帖子列表
      </div>
      <div class="desc">
          在这里管理所有帖子，并对帖子进行各种操作
      </div>

      <el-table :data="topicList.list" height="400">
          <el-table-column prop="id" label="帖子ID" width="80" align="center"/>
          <el-table-column prop="title" label="标题" width="300" show-overflow-tooltip/>
          <el-table-column label="帖子类型" width="120">
              <template #default="{ row }">
                  <div class="topic-type">
                      <div class="type-dot" :style="{backgroundColor:findType(row.type)?.color ?? '#bababa'}"></div>
                      <div>{{findType(row.type)?.name ?? "未知类型"}}</div>
                  </div>
              </template>
          </el-table-column>
          <el-table-column label="帖子作者" width="150">
              <template #default="{ row }">
                  <div class="topic-username">
                      <el-avatar :size="25" :src="store.avatarUserUrl(row.avatar)"/>
                      <div>{{ row.username }}</div>
                  </div>
              </template>
          </el-table-column>
          <el-table-column prop="time" label="发布时间" width="180" align="center"
            :formatter="row => new Date(row.time).toLocaleString()"/>

          <el-table-column label="操作" width="270" fixed="right" align="center">
              <template #default="{ row }">
                  <el-button size="small" type="info" @click="invisibleTopic(row.id,false)" v-if="row.invisible">取消</el-button>
                  <el-button size="small" type="info" @click="invisibleTopic(row.id,true)" v-else plain>屏蔽</el-button>
                  <el-button size="small" type="warning" @click="lockTopic(row.id,false)" v-if="row.locked">取消</el-button>
                  <el-button size="small" type="warning" @click="lockTopic(row.id,true)" v-else plain>锁定</el-button>
                  <el-button size="small" type="success" @click="topTopic(row.id,false)" v-if="row.top">取消</el-button>
                  <el-button size="small" type="success" @click="topTopic(row.id,true)" plain v-else>置顶</el-button>
                  <el-button size="small" type="danger" plain @click="deleteTopic(row.id)">删除</el-button>
              </template>
          </el-table-column>
      </el-table>

      <div class="pagination">
          <el-pagination :total="topicList.total"
                         v-model:current-page="topicList.page"
                         v-model:page-size="topicList.size"
                         layout="total, sizes, prev, pager, next, jumper"/>
      </div>
      <div class="prohibited-input">
          <div class="title">违禁词管理</div>
          <div class="desc">所有包含违禁词的帖子与评论都被限制发布，使用逗号隔开</div>
          <el-input type="textarea" :rows="8" v-model="prohibitedWords"/>
          <div style="text-align:right;margin-top: 20px">
              <el-button type="primary" @click="saveProhibitedWord">保持违禁词列表</el-button>
          </div>

      </div>

  </div>
</template>

<style lang="less" scoped>
.forum-admin{
    .title{
        font-weight: bold;
    }
    .desc{
        color:#bababa;
        font-size:13px;
        margin-left:20px;
    }
    .pagination{
        margin-top:20px;
        display: flex;
        justify-content: right;
    }

    .topic-username{
        height: 30px;
        display: flex;
        align-items: center;
        gap: 15px;
    }

    .topic-type{
        gap:10px;
        display: flex;
        align-items: center;
        .type-dot{
            height: 7px;
            width: 7px;
            border-radius: 50%;
        }
    }

    .prohibited-input{
        margin-top: 40px;

    }
}
</style>