<template>
  <PlayGround v-if="$store.state.pk.status === 'playing'" />
  <MatchGround v-else />
</template>

<script>
import PlayGround from "@/components/PlayGround.vue";
import MatchGround from "@/components/MatchGround.vue";
import { onMounted, onUnmounted } from "vue";
import { useStore } from "vuex";

export default {
  components: {
    PlayGround,
    MatchGround,
  },
  setup() {
    const store = useStore();

    let socket = null;
    let socket_url = `ws://localhost:3000/websocket/${store.state.user.jwt_token}/`;

    onMounted(() => {
      socket = new WebSocket(socket_url);

      store.commit("updateOpponent", {
        username: "我的对手",
        photo: "https://cdn.acwing.com/media/article/image/2022/08/09/1_1db2488f17-anonymous.png",
      });

      socket.onopen = () => {  // 链接成功建立后会执行
        console.log("Connected!");
        store.commit("updateSocket", socket);
      };

      socket.onmessage = (msg) => {  // 接收到后端消息时会执行
        const data = JSON.parse(msg.data);  // Spring传过来的数据是放在消息的data中
        console.log(data);

        if (data.event === "match_success") {  // 匹配成功
          store.commit("updateOpponent", {  // 更新对手信息
            username: data.opponent_username,
            photo: data.opponent_photo,
          });
          store.commit("updateGameMap", data.game_map);  // 更新游戏地图

          setTimeout(() => {  // 3秒后再进入游戏地图界面
            store.commit("updateStatus", "playing");
          }, 3000);
        }
      };

      socket.onclose = () => {  // 关闭链接后会执行
        console.log("Disconnected!");
        store.commit("updateStatus", "matching");  // 进入游戏地图后玩家点击其他页面应该是默认退出游戏
      };
    });

    onUnmounted(() => {
      socket.close();  // 如果不断开链接每次切换页面都会创建新链接，就会导致有很多冗余链接
    });
  },
};
</script>

<style scoped></style>
