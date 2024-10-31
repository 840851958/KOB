<template>
    <div class="playground"> 
        <GameMap />
    </div>

</template>

<script>
import GameMap from "@/components/GameMap.vue";

export default {
    components: {
        GameMap
    }
}

</script>

<style scoped>
div.playground {
    /* 这样当浏览器发生变化，整个图形也会变化 */
    width: 60vw;
    height: 70vh;
    /* background-color: blue; */
    margin: 40px auto;
}
</style>