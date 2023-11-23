<template>
  <div class="matchground">
    <div class="row">
      <div class="col-md-6" style="text-align: center;">
        <div class="photo">
          <img class="img-fluid" :src="$store.state.user.photo">
        </div>
        <div class="username">
          {{ $store.state.user.username }}
        </div>
      </div>
      <div class="col-md-6" style="text-align: center;">
        <div class="photo">
          <img class="img-fluid" :src="$store.state.pk.opponent_photo">
        </div>
        <div class="username">
          {{ $store.state.pk.opponent_username }}
        </div>
      </div>
      <div class="col-md-12 text-center" style="margin-top: 14vh;">
        <button @click="click_match_btn" type="button" class="btn btn-info btn-lg">
          {{ match_btn_info }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from "vue";
import { useStore } from "vuex";

export default {
  setup() {
    const store = useStore();
    let match_btn_info = ref("开始匹配");

    const click_match_btn = () => {
      if (match_btn_info.value === "开始匹配") {
        match_btn_info.value = "取消";
        store.state.pk.socket.send(JSON.stringify({  // 将json封装成字符串发送给后端，后端会在onMessage()中接到请求
          event: "start_match",  // 表示开始匹配
        }));
      } else {
        match_btn_info.value = "开始匹配";
        store.state.pk.socket.send(JSON.stringify({
          event: "stop_match",  // 表示停止匹配
        }));
      }
    };

    return {
      match_btn_info,
      click_match_btn,
    };
  },
};
</script>

<style scoped>
div.matchground {
  width: 60vw;
  height: 70vh;
  margin: 40px auto;
  border-radius: 10px;
  background-color: rgba(50, 50, 50, 0.5);
}

img {
  width: 35%;
  border-radius: 50%;
  margin: 14vh 0 1vh 0;
}

.username {
  font-size: 24px;
  font-weight: bold;
  color: white;
}
</style>
