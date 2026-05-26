import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useCartStore = defineStore('cart', () => {
  const items = ref(JSON.parse(localStorage.getItem('cart') || '[]'))

  const totalCount = computed(() => items.value.reduce((sum, item) => sum + item.quantity, 0))
  const totalPrice = computed(() => items.value.reduce((sum, item) => sum + item.price * item.quantity, 0))
  const selectedItems = computed(() => items.value.filter((item) => item.selected))
  const selectedTotalPrice = computed(() =>
    selectedItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
  )

  function addItem(product) {
    const existing = items.value.find((item) => item.productId === product.id)
    if (existing) {
      existing.quantity += 1
    } else {
      items.value.push({
        productId: product.id,
        name: product.name,
        price: product.price,
        image: product.images ? JSON.parse(product.images)[0] : '',
        stock: product.stock || 999,
        quantity: 1,
        selected: true,
      })
    }
    save()
  }

  function removeItem(productId) {
    items.value = items.value.filter((item) => item.productId !== productId)
    save()
  }

  function updateQuantity(productId, quantity) {
    const item = items.value.find((item) => item.productId === productId)
    if (item) {
      item.quantity = Math.max(1, Math.min(quantity, item.stock))
      save()
    }
  }

  function toggleSelect(productId) {
    const item = items.value.find((item) => item.productId === productId)
    if (item) {
      item.selected = !item.selected
      save()
    }
  }

  function clearSelected() {
    items.value = items.value.filter((item) => !item.selected)
    save()
  }

  function clearAll() {
    items.value = []
    save()
  }

  function save() {
    localStorage.setItem('cart', JSON.stringify(items.value))
  }

  return {
    items, totalCount, totalPrice, selectedItems, selectedTotalPrice,
    addItem, removeItem, updateQuantity, toggleSelect, clearSelected, clearAll,
  }
})
