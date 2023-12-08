<template>
  <PlayGround />
</template>

<script>
import PlayGround from "@/components/PlayGround.vue";
import { onMounted } from "vue";
import { useStore } from "vuex";

export default {
  components: {
    PlayGround,
  },
  setup() {
    const store = useStore();

    onMounted(() => {
      store.commit("updateStatus", "local");
    });
  },
};
</script>

<style scoped></style>
