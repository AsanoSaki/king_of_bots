<template>
  <div class="card text-bg-secondary text-center">
    <div class="card-header" style="font-size: 26px;">
      游戏结束
    </div>
    <div class="card-body" style="background-color: rgba(255, 255, 255, 0.4);">
      <div class="result_board_text" v-if="$store.state.pk.loser === 'all'">
        Draw
      </div>
      <div class="result_board_text" v-else-if="$store.state.pk.loser === 'A' && $store.state.pk.a_id.toString() === $store.state.user.id">
        Lose
      </div>
      <div class="result_board_text" v-else-if="$store.state.pk.loser === 'B' && $store.state.pk.b_id.toString() === $store.state.user.id">
        Lose
      </div>
      <div class="result_board_text" v-else>
        Win
      </div>
      <div class="result_board_btn">
          <button @click="returnHome" type="button" class="btn btn-info btn-lg">
            返回主页
          </button>
      </div>
    </div>
  </div>
</template>

<script>
import { useStore } from "vuex";

export default {
  setup() {
    const store = useStore();

    const returnHome = () => {  // 需要复原一些全局变量
      store.commit("updateStatus", "matching");
      store.commit("updateLoser", "none");
      store.commit("updateOpponent", {
        username: "我的对手",
        photo: "https://cdn.acwing.com/media/article/image/2022/08/09/1_1db2488f17-anonymous.png",
      });
    };

    return {
      returnHome,
    };
  },
};
</script>

<style scoped>
.card {
  width: 30vw;
  position: absolute;
  top: 25vh;
  left: 35vw;
}

.result_board_text {
    color: white;
    font-size: 50px;
    font-weight: bold;
    font-style: italic;
    padding: 5vh 0;
}

.result_board_btn {
    padding: 3vh 0;
}
</style>
