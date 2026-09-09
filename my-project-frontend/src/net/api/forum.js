import {get, post} from "@/net";
import {ElMessage} from "element-plus";

export const apiForumTypes = (success) =>
    get('/api/forum/types', success)

export const apiForumTopic = (tid, success) =>
    get(`api/forum/topic?tid=${tid}`, success)

export const apiForumInteract = (tid, type, topic, message) => {
    get(`/api/forum/interact?tid=${tid}&type=${type}&state=${!topic[type]}`, () => {
        topic[type] = !topic[type]
        if(topic[type])
            ElMessage.success(`${message}成功！`)
        else
            ElMessage.success(`已取消${message}！`)
    })
}

