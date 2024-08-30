<!-- src/components/common/BaseModal.vue -->
<template>
  <div class="modal" v-if="visible">
    <div class="modal-content">
      <header class="modal-header">
        <slot name="header">Default Header</slot>
        <button class="close" @click="closeModal">&times;</button>
      </header>
      <section class="modal-body">
        <slot name="body">Default Body</slot>
      </section>
      <footer class="modal-footer">
        <slot name="footer">
          <button @click="closeModal">Close</button>
        </slot>
      </footer>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
})

const emits = defineEmits(['close'])

const closeModal = () => {
  emits('close')
}
</script>

<style scoped>
.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-content {
  background: white;
  padding: 20px;
  border-radius: 5px;
  width: 500px;
  max-width: 90%;
}

.modal-header,
.modal-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.close {
  cursor: pointer;
  font-size: 1.5em;
}
</style>
