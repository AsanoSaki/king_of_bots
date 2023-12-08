<template>
  <div class="container">
    <div class="card text-bg-secondary" style="margin: 40px auto; width: 85%;">
      <div class="card-header" style="font-size: 4vh; font-weight: bold; font-style: italic;">
        <span>匹配对战</span>
        <transition name="fade">
          <div v-show="show_match_time" class="float-end">{{ match_time }}</div>
        </transition>
      </div>
      <div class="card-body" style="background-color: rgba(255, 255, 255, 0.5);">
        <div class="row">
          <div class="col-md-6">
            <div class="card user-card">
              <div class="card-body text-center photo">
                <img class="img-fluid" :src="$store.state.user.photo">
              </div>
              <div class="card-footer text-center">
                <div class="username">
                  {{ $store.state.user.username }}
                </div>
              </div>
            </div>
          </div>
          <div class="col-md-6">
            <div class="card user-card">
              <div class="card-body text-center photo">
                <img class="img-fluid" :src="$store.state.pk.opponent_photo">
              </div>
              <div class="card-footer text-center">
                <div class="username">
                  {{ $store.state.pk.opponent_username }}
                </div>
              </div>
            </div>
          </div>
          <div class="col-md-12 text-center" style="margin: 8vh 0 2vh 0;">
            <button @click="click_match_btn" type="button" class="btn btn-info btn-lg">
              {{ match_btn_info }}
            </button>
          </div>
        </div>
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
    let match_time = ref(0);
    let show_match_time = ref(false);
    let timerId = null;
    let interval = 1000;
    let expected = 0;

    const count = () => {
      show_match_time.value = true;
      setTimeout(() => {
        show_match_time.value = false;
        setTimeout(() => {
          match_time.value++;
        }, 500);
      }, 500);

      let offset = Date.now() - expected;
      let nextTime = interval - offset;
      expected += interval;
      timerId = setTimeout(count, nextTime);
    };

    const click_match_btn = () => {
      if (match_btn_info.value === "开始匹配") {
        match_btn_info.value = "取消";

        expected = Date.now() + interval;
        setTimeout(count, interval);

        store.state.pk.socket.send(JSON.stringify({  // 将json封装成字符串发送给后端，后端会在onMessage()中接到请求
          event: "start_match",  // 表示开始匹配
        }));
      } else {
        match_btn_info.value = "开始匹配";
        show_match_time.value = false;

        setTimeout(() => {  // 需要等完全淡出才能重置数字
          match_time.value = 0;
        }, 500);
        clearTimeout(timerId);

        store.state.pk.socket.send(JSON.stringify({
          event: "stop_match",  // 表示停止匹配
        }));
      }
    };

    return {
      match_btn_info,
      click_match_btn,
      match_time,
      show_match_time,
    };
  },
};
</script>

<style scoped>
img {
  width: 85%;
  border-radius: 50%;
  margin: 2vh 0 2vh 0;
}

.user-card {
  width: 50%;
  margin: 8vh auto 0;
}

.photo {
  background-color: rgba(0, 0, 0, 0.1);
}

.card-footer {
  background-color: rgba(0, 0, 0, 0.4);
}

.username {
  font-size: 24px;
  font-weight: bold;
  color: white;
}

.fade-enter-active, .fade-leave-active {
  transition: opacity .5s;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

.fade-enter-to, .fade-leave-from {
  opacity: 1;
}
</style>
