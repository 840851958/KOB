<template>
<nav class="navbar navbar-expand-lg bg-body-tertiary">
  <div class="container">
    <!-- <routerlink>局部刷新、<a>全局刷新 -->
    <router-link class="navbar-brand" :to="{name: 'home'}"> King of Bots </router-link>
    <div class="collapse navbar-collapse" id="navbarText">
      <ul class="navbar-nav me-auto mb-2 mb-lg-0">
        <li class="nav-item">
          <router-link :class="route_name == 'pk_index' ? 'nav-link active' : 'nav-link'" :to="{name: 'pk_index'}"> PK </router-link>
          <!-- <a class="nav-link" aria-current="page" href="/pk/">PK </a> -->
        </li>
        <li class="nav-item">
          <router-link :class="route_name == 'record_index' ? 'nav-link active' : 'nav-link'" :to="{name: 'record_index'}"> 对局列表 </router-link>
          <!-- <a class="nav-link" href="/record/"> 对局列表</a> -->
        </li>
        <li class="nav-item">
          <router-link :class="route_name == 'ranklist_index' ? 'nav-link active' : 'nav-link'" :to="{name: 'ranklist_index'}"> 排行榜 </router-link>
        </li>
      </ul>
      <!-- 讨论用户登录前后个人信息的显示 -->
      <ul class="navbar-nav" v-if="$store.state.user.is_login">
        <li class="nav-item dropdown">
          <a class="nav-link dropdown-toggle" href="/user/" role="button" data-bs-toggle="dropdown" aria-expanded="false">
            {{ $store.state.user.username }}
          </a>
          <ul class="dropdown-menu">
            <li><router-link class="dropdown-item" :to="{name: 'user_bot_index'}">我的Bot</router-link></li>
            <li><hr class="dropdown-divider"></li>
            <li><a class="dropdown-item" href="#" @click="logout">退出登录</a></li>
          </ul>
        </li>
      </ul>
      <ul class="navbar-nav" v-else-if="!$store.state.user.pulling_info">
        <li class="nav-item">
          <router-link class="nav-link" :to="{name: 'user_account_login'}" role="button">
            登录
          </router-link>
        </li>
        <li class="nav-item">
          <router-link class="nav-link" :to="{name: 'user_account_register'}" role="button">
            注册
          </router-link>
        </li>
      </ul>
    </div>
  </div>
</nav>
</template>



<script>
import { useRoute } from 'vue-router';
import { computed } from 'vue';
import { useStore } from 'vuex';

export default {
  // 构造函数，返回当前route的名称，template中调用判断高亮
  setup() {
    const store = useStore();
    const route = useRoute();
    let route_name = computed(() => route.name)
    store.commit("updatePullingInfo", false); 

    const logout = () => {
      store.dispatch("logout");
    }

    return{
      route_name,
      logout
    }
  }
}
</script>




<style scoped>

</style>