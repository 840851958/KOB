<template>
  <NavBar />
  <router-view></router-view>
</template>

<script>
  import NavBar from "./components/NavBar.vue"
  import "bootstrap/dist/js/bootstrap"
  import "bootstrap/dist/css/bootstrap.min.css"



  export default {
    components: {
      NavBar
    },
    setup(){
/*       $.ajax({

      });
      $.ajax({
        
      });
      $.ajax({
        url: "http://localhost:3000/user/account/register/",
        type: "post",
        data: {
          username: "test1",
          password: "test",
          confirmedPassword: "test",
        },
        success(resp){
          console.log(resp);
        },
        error(resp){
          console.log(resp);
        }
      })  */
    }
}
</script>

<style>
  body {
    background-image: url(./assets/images/111.jpg);
    background-size: cover;
  }
</style>
