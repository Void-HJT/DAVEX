<template>
  <router-view></router-view>
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue'
import { useNotificationStore } from './stores/index'

const applicationId = 'DAVEX-C1-A1'
const notificationStore = useNotificationStore()

onMounted(() => {
  notificationStore.startPolling(applicationId)
})

onUnmounted(() => {
  notificationStore.stopPolling()
})
</script>

<style scoped>
body {
  font-family: "Microsoft YaHei", Arial, sans-serif;
}
</style>
