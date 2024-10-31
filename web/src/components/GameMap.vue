<!-- 计分板-->
<template>
    <div ref="parent" class="gamemap">
        <!-- canvas加入tabindex属性可以获取键盘输入 -->
        <canvas ref="canvas" tabindex="0"> </canvas>
    </div>
</template>

<script>
import { GameMap } from "@/assets/scripts/GameMap";
import { ref, onMounted} from "vue"

export default {
    setup(){
        let parent = ref(null);
        let canvas = ref(null);

        //挂载?
        onMounted(() => {
            new GameMap(canvas.value.getContext('2d'), parent.value);
        });

        
        return{
            parent,
            canvas
        }
    }
}

</script>

<style scoped> 
div.gamemap {
    width: 100%;
    height: 100% ;
    display: flex;
    justify-content: center;
    align-items: center;
}
</style>
