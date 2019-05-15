<template>
    <p class="no-auto-box">
        <img src="../images/no-auth.png" alt="">
    </p>
</template>
<style lang="less">
html,body,#app {
  padding: 0;
  margin: 0;
  height: 100%;
  width: 100%; 
  .no-auto-box {
    display: flex;
    padding: 0;
    margin: 0;
    height: calc(~'100% - 160px');
    width: 100%;
    img {
        margin: auto;
    }
  }
}
</style>
