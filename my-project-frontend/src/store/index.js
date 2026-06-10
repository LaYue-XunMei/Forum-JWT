import{defineStore} from "pinia"
import axios from "axios";

export const userStore = defineStore("general",{
    state:() => {
        return{
            user:{
                username: '',
                email: '',
                role: '',
                avatar: null,
                registerTime: null
            }
        }
    },getters: {
        avatarUrl() {
            if(this.user.avatar)//如果已经设置了头像
                return `${axios.defaults.baseURL}/images${this.user.avatar}`
            else
                return 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
        }
    }
})