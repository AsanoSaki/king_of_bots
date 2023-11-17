<template>
  <NavBar />
  <router-view />
</template>

<script>
import NavBar from "@/components/NavBar.vue";

export default {
  components: {
    NavBar,
  },
  setup() {
    // $.ajax({
    //   url: "http://localhost:3000/user/account/register/",
    //   type: "POST",
    //   data: {
    //     username: "user7",
    //     password: "123456",
    //     confirmedPassword: "123456",
    //   },
    //   success(resp) {
    //     console.log(resp);
    //   },
    //   error(resp) {
    //     console.log(resp);
    //   },
    // });
  },
};
</script>

<style>
body {
  background-image: url("@/assets/images/background.png");
  background-size: cover;
}
</style>
