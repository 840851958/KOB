import { createStore } from 'vuex'
// 利用Module将全局变量进行分类
import ModuleUser from "./user"
import ModulePk from "./pk"

export default createStore({
  state: {
  },
  getters: {
  },
  mutations: {
  },
  actions: {
  },
  modules: {
    user: ModuleUser,
    pk: ModulePk, 
  }
})
