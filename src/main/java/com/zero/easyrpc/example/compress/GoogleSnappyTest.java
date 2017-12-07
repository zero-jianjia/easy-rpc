package com.zero.easyrpc.example.compress;

import java.io.IOException;
import java.io.UnsupportedEncodingException;


/**
 * 
 * @author BazingaLyn
 * @description Google的Snappy的测试
 * @time 2016年10月18日
 * @modifytime
 */
public class GoogleSnappyTest {
	
	public static void main(String[] args) throws UnsupportedEncodingException, IOException {
			String request = "人来人往的机场大厅，赵默笙坐在大厅的椅子上休息，一个年轻女人来到赵默笙身边坐下，一不小心碰掉了赵默笙手中的背包，赵默笙的背包里面放着非常贵重的相机，年轻女子一脸愧疚提醒赵默笙赶紧检查一下背包，赵默笙在年轻女子的提醒下打开背包拿出一台相机仔细检查，相机完好无损没有问题，赵默笙将相机放回到背包里面没有再为难年轻女子，年轻女子见识出赵默笙手中的相机非常昂贵，脸上露出一丝惊讶猜出赵默笙是一名摄影师，一般摄影师都是男性居多，年轻女子一脸惊讶对赵默笙产生了敬意，赵默笙性格内向没有跟年轻女子闲聊，年轻女子坐在赵默笙身边涛涛不绝讲述在美国生活的情景。何以琛来到一家公司跟客户开会，开会的客户是一名洋人，洋人向何以琛介绍助手许影，许影与何以琛曾是大学同学，两人当场亲密握手，洋人一脸惊讶方才意识到何以琛与许影认识。会议结束何以琛离开会议室遇到妹妹何以玫，许影不知道何以玫与何以琛是兄妹关系，还以为何以琛跟何以玫是恋人关系，何以琛见许影产生误会，只得主动公布跟何以玫是兄妹关系，许影见何以琛与何以玫不是情侣，脸上露出笑容非常开心，何以玫的面色看起来却开始变得不太自然。赵默笙来到一所杂志社面试，杂志社的老总是一个中年女子，中年女子笑容满面与赵默笙谈话，赵默笙给中年女子的感觉文艺时尚，中年女子当场提醒赵默笙可以来杂志社工作。何以玫在开车过程中提议跟何以琛晚上逛街看电影，何以琛没有心情陪何以玫看电影，何以玫心中升起不悦加快车速开车险些撞到一个年轻男子，年轻男子的名字叫路远风，路远风一眼认出何以玫，何以玫在市内小有名气主持过电视节目，路远风惊喜交加向何以玫索要签名，何以玫二话不说写下签名送给路远风。路远风得到何以玫的签名回到杂志社工作，赵默笙成为了路远风的新同事，路远风向上级讲述之前险遇车祸受伤的经过。赵默笙晚上逛超市购物意外遇到何以琛，何以琛与何以玫正在超市里面购物，赵默笙惊喜交加看着何以琛，何以琛却像是没有认出赵默笙一样一声不吭离去。赵默笙与同事路远风为第一个客户拍照，第一个客户的名字叫少梅是赵默笙的同学，赵默笙拍完照总觉得没有拍好，路远风劝说赵默笙没有必要过份认真工作，两人说话的时候少梅一边走出摄影室一边提醒赵默笙一起到外面喝咖啡。";
			request += "人来人往的机场大厅，赵默笙坐在大厅的椅子上休息，一个年轻女人来到赵默笙身边坐下，一不小心碰掉了赵默笙手中的背包，赵默笙的背包里面放着非常贵重的相机，年轻女子一脸愧疚提醒赵默笙赶紧检查一下背包，赵默笙在年轻女子的提醒下打开背包拿出一台相机仔细检查，相机完好无损没有问题，赵默笙将相机放回到背包里面没有再为难年轻女子，年轻女子见识出赵默笙手中的相机非常昂贵，脸上露出一丝惊讶猜出赵默笙是一名摄影师，一般摄影师都是男性居多，年轻女子一脸惊讶对赵默笙产生了敬意，赵默笙性格内向没有跟年轻女子闲聊，年轻女子坐在赵默笙身边涛涛不绝讲述在美国生活的情景。何以琛来到一家公司跟客户开会，开会的客户是一名洋人，洋人向何以琛介绍助手许影，许影与何以琛曾是大学同学，两人当场亲密握手，洋人一脸惊讶方才意识到何以琛与许影认识。会议结束何以琛离开会议室遇到妹妹何以玫，许影不知道何以玫与何以琛是兄妹关系，还以为何以琛跟何以玫是恋人关系，何以琛见许影产生误会，只得主动公布跟何以玫是兄妹关系，许影见何以琛与何以玫不是情侣，脸上露出笑容非常开心，何以玫的面色看起来却开始变得不太自然。赵默笙来到一所杂志社面试，杂志社的老总是一个中年女子，中年女子笑容满面与赵默笙谈话，赵默笙给中年女子的感觉文艺时尚，中年女子当场提醒赵默笙可以来杂志社工作。何以玫在开车过程中提议跟何以琛晚上逛街看电影，何以琛没有心情陪何以玫看电影，何以玫心中升起不悦加快车速开车险些撞到一个年轻男子，年轻男子的名字叫路远风，路远风一眼认出何以玫，何以玫在市内小有名气主持过电视节目，路远风惊喜交加向何以玫索要签名，何以玫二话不说写下签名送给路远风。路远风得到何以玫的签名回到杂志社工作，赵默笙成为了路远风的新同事，路远风向上级讲述之前险遇车祸受伤的经过。赵默笙晚上逛超市购物意外遇到何以琛，何以琛与何以玫正在超市里面购物，赵默笙惊喜交加看着何以琛，何以琛却像是没有认出赵默笙一样一声不吭离去。赵默笙与同事路远风为第一个客户拍照，第一个客户的名字叫少梅是赵默笙的同学，赵默笙拍完照总觉得没有拍好，路远风劝说赵默笙没有必要过份认真工作，两人说话的时候少梅一边走出摄影室一边提醒赵默笙一起到外面喝咖啡。";
			request += "少梅是赵默笙的大学同学，成名之后少梅改名为箫筱，箫筱与赵默笙业务合作故意提出停止合作，当年赵默笙狠心扔下何以琛出国，箫筱依然记得当年何以琛因为赵默笙出国茶饭不思如同丢了三魂六魄。为了教训一下赵默笙，箫筱故意提出跟赵默笙的公司结束合作，路远风见箫筱无原无故取消合作，脸上升起不悦与箫筱发生争吵，箫筱一副高高在上的姿态就是不愿意再合作，路远风拿出合约书提醒箫筱无故结束合作等同违约，箫筱趁着路远风不注意夺过合约书撕得粉碎，路远风惊怒交加看着箫筱，箫筱声称事后要控告路远风的公司。路远风与赵默笙回到杂志社向张主编汇报与箫筱吵架的经过，张主编愤愤不平决定主动控告箫筱，箫筱为人蛮不讲理已经得罪过很多公司，张主编心知就算她不主动控告箫筱，箫筱也会主动控告杂志社。正如张主编猜测的一样，箫筱在经纪人凯文的陪同下来到律师事务所找何以琛求助，何以琛是箫筱的法律顾问，箫筱要求何以琛控告张主编，何以琛见箫筱又跟客户发生纠纷，脸上升起不悦盘问箫筱为何控告张主编，箫筱不愿把原因说出来，何以琛一本正经提醒箫筱跟他合约结束之后就不会再续约，箫筱见何以琛不愿意再做她的法律顾问，脸上升起不悦只得在凯文的陪同下离去。赵默笙来到何氏律师事务所想找何以琛，由于不知道律师事务所的律师就是何以琛，赵默笙走进律师事务所向前台工作人员提出找何律师谈事情，何以琛出门办事不在律师事务所，赵默笙只得在一家餐厅等待何以琛。何以琛开车来到餐厅外面不愿意跟赵默笙见面，而是打电话让手下人来餐厅从赵默笙手中拿走资料，赵默笙送出资料离开餐厅冒雨回家，何以琛从在汽车里面目不转睛盯着赵默笙从汽车外面经过。赵默笙离去不久，何以琛回到办公室查看赵默笙转送的资料，何以玫来到办公室接何以琛一眼看到资料上写着赵默笙的名字。文总监是赵默笙的上司，赵默笙因为跟箫筱闹不和影响杂志社推出新期刊，文总监因为工作无法顺利开展训了赵默笙一顿。赵默笙为了完成任务邀请一名知名洋人男模拍摄相片，张主编对赵默笙拍摄的相片非常满意，叮嘱赵默笙不要再担心其它不相关的事情，把所有精神放在工作上便可。何以琛晚上来到超市购物，在购物过程中何以琛想起不久之前与赵默笙在超市相遇的经过，当时赵默笙一脸震惊看着何以琛，何以琛神色复杂盯着赵默笙一会儿扬长离去。";
			request += "默笙下班回到家发现灯泡坏了，只得来到超市购买。超市保安看到她发现似曾相识，想起之前捡到皮夹里夹的照片似乎就是眼前这位姑娘，他拿来皮夹问默笙这是不是她的，默笙说不是自己的，但保安坚持让她打开皮夹看看那照片是不是她，默笙一看照片确实是自己的，但推说皮夹不是自己的坚持不能拿，保安说既然皮夹的主人藏着她的照片，必然跟她有关联，说不定自己还能促成一段好姻缘呢。默笙回家打开皮夹，费起了思量，如果说这皮夹是以琛的，但他如今似乎都不想见自己了，怎么还会放着他的照片？她翻过照片看到背后的留言“my sunshine”，笔迹确实是以琛的。默笙决定将皮夹送去袁向何律师事务所，以琛不在，默笙将皮夹交给美婷，让她转交。主编宣布一个好消息，因为封面启用了大卫摩根，本期杂志的销售特别好，销量超过往期50%，并且仍在突破，大家得知这一消息都十分兴奋。事务所的两个小律师记错了开庭时间，被何以琛一顿批评。袁律师拿着皮夹来找何以琛八卦，让他承认自己在外面干了什么骗财骗色的事？为什么害得人家小姑娘一直躲在咖啡馆等他出门才敢把东西送来？以琛打开皮夹发现照片不见了，他知道皮夹是默笙送来的。他不禁回想起两人的第一次见面，那时的默笙就是一个死缠烂打的小丫头，只为了知道自己的名字，不知羞地追着自己走。小红三八地赶来告诉默笙有人找，还垂涎地说是个帅哥哦。来人正是以琛，一见面以琛公事公办地叫她“赵小姐”，默笙只得犹豫地叫他“何先生”。以琛告诉默笙自己是萧筱的律师，自己此行除了公事还想要回自己皮夹里的照片，默笙说照片里的人是自己，以琛劝默笙不要跟一个律师讨论物品所有权的问题，自己要拿回照片是想时时提醒自己不要再上当，临走时以琛说明天会来取，但默笙答应自己会送过去。参加完同事的聚餐回家，默笙独自走在大街上，不禁回想起刚考上大学入学的那天，那天是他第一次见以琛，默笙坚持不要老爸送她到宿舍区，她边走边拍着风景，看到在大树下专心读书的何以琛，她偷偷拍他，被发现了还狡辩自己在拍风景，以琛起身说把风景还给她，默笙追着以琛走，非得知道他的名字。默笙来到新生宿舍见到新同学十分亲热，建议大家一起合影。默笙对以琛一见钟情，在校园里到处可以看到默笙追着以琛走的情景，她巴巴地给以琛送照片，以琛收下照片，她又缠着人家请他吃饭。她一次次地制造着偶遇，但以琛总是对她冷冷的。";
			request += "少梅看到以琛走了留下默笙一人在吃饭，还过来安慰她不要难过，但默笙反而高兴得很，说是自己的作战计划成功，以琛不仅记住了自己的名字，而且记得很牢呢。默笙来到法学院抄以琛的课程表，以琛问她为什么老是跟着自己，而且自己在大学里不准备找女朋友，默笙连忙说自己可以先排队。默笙在图书馆借法学的书，偶遇以琛，她赶紧发誓自己不是尾随他而来的，以琛告诉她如果选修课的话她借的书太难了。以琛在寝室里看着默笙给他拍的照片，被同学发现，问他何时有这闲情逸致，他只说是一个意外。学校辩论社招聘，默笙也去报名，她由于摄影特长被录取了，一度受到质疑，但以琛作为副社长认为不一定法学系的才适合进辩论社，他同意录取赵默笙。辩论社的许影副社长教育赵默笙，不要把辩论社当成一年级女生追男生的场所，没想到赵默笙厚脸皮地说，自己一定会在社内好好工作，社外认真追人的。把许副社长气得不轻。默笙替辩论社的社员送盒饭，来到食堂看到一堆的盒饭犯了愁，正好看到何以琛和另一同学在附近，她赶忙招呼他们帮自己看着盒饭，自己先送一趟再回来取，她一边说着还一边打着喷嚏。以琛看着冷冷淡淡的，却默默地买了一盒三九感冒灵给默笙。少梅浑身是伤地被搀回寝室，她伤心地说自己打工的钱全被抢了，但不出去打工自己下学期的学费都没有着落，默笙心疼地对少梅说，如果一定要打工，自己下次陪她一起去。辩论社开会，许影说赵默笙已经好多天没来社团了，自己早说过一年级的新生能有什么毅力啊。何以琛推说自己还要去实习的律师事务所，提前走了，他陪着律师在咖啡厅见客，发现默笙居然也在这里，默笙说少梅在附近打工，自己怕她晚上回去不安全，所以在这里等她，以琛说她消费的金额比少梅打工挣的还多，自己最近会在律所实习，如果少梅不介意的话自己可以陪少梅一起回学校，默笙不愿意以琛陪着少梅，以琛无奈就让她每天晚上到他实习的律所自习，把默笙乐得不行。没想到少梅怕耽误默笙的正事已经把工作辞了，默笙一听急了，拖着少梅回去找老板说情，但老板已经找到了顶替的员工回绝了她们。默笙想着以琛反正也不知道少梅辞职的事，于是照旧每天按约好的时间和以琛见面。一天下着大雨，他们一起帮助了一个需要法律援助的人后发现时间已晚，何以琛提醒少梅该下班了，默笙硬着头皮打电话回寝室，然后告诉以琛少梅已经自己先回家了，机敏的以琛早就发现了默笙在说谎，他送默笙回到学校，默笙心虚地坦白，争取从宽处理，她当然没有看到转身而走的以琛露出了微笑。默笙来到辩论社被许影质疑，以琛替她说话，默笙高兴地说一定会把何师兄拍得很帅的。辩论社外出比赛，默笙做着服务工作，站在车下发早餐，她拜托以琛帮她留个靠前的位置，自己晕车，以琛突感身体不适，说是可能昨晚吃了羊肉串引起的。到了比赛场地以琛发现自己比赛用的提示卡不见了，许影趁机责怪是默笙坏事，但以琛自信地说一个需要提示卡才能赢的选手不是一个好辩手。比赛在紧张地进行着，赵默笙在场下卖力地拍摄着。正在台上作着总结陈词的以琛突然忘词了。";
			request += "以琛迅速调整状态，非常出色地完成了结案陈词，场下爆发热烈的掌声。比赛结束默笙发现以琛不见了，许影故意大声地说是被对方辩手约出去表白了，默笙着急地找了出去。默笙在寝室里继续钻研《经济法》，希望能和以琛多一些共同语言，室友回来说外面都在传说以琛的女朋友是赵默笙呢，要她请客。默笙不知谣言从何而起，担心以琛知道了会生气，她急急地找以琛去澄清，说这谣言不是自己传出去的，但以琛说知道，因为这消息是自己放出去的，因为如果注定三年后默笙是自己的女朋友，那何不提早行使自己的权利呢，默笙高兴得跳了起来。从此默笙就成了以琛的小尾巴，一起自习、一起上课，温馨而甜蜜。许影喜欢以琛，所以处处针对默笙，但以琛却总是呵护着她。一次许影让默笙替辩论社的同学买盒饭，买回来却非说默笙少买了一盒，以琛息事宁人把自己的饭让出来，自己和默笙分一份饭吃，没想到这更惹怒了许影。许影趁着默笙出来扔垃圾向她示威，说默笙得到的只不过是一个穷鬼，连出国交换生的名额也因为交不出学费而让给她，一向温顺的默笙发怒了，她斥责许影不配得到交换生的名额。以琛听到了她们的对话，心里默默发誓一定会让默笙过上好日子的。期末考试结束了，就要放寒假了，因为两家都是宜市人，所以默笙丝毫没有离愁别绪，她觉得寒假里两人也可以天天见面的。但以琛拒绝将家里的电话号码告诉默笙。默笙回到家闷闷不乐，老爸安慰她，告诉她以琛一定是因为太爱她了，出于男人的自尊才不让她去找他的，默笙瞬间就高兴了，决定到大街上去制造偶遇，果不其然，在大街上她遇到了和妹妹何以玫一起逛街的以琛，她兴奋地冲上前去扯着以琛的袖子，她跟以琛约好一起返校。以玫问以琛不是说过大学里不找女朋友的嘛？以琛告诉她默笙是自己送上门来的，以玫松了口气，但她忽略了以琛说起默笙时眼睛里荡漾的笑意。默笙在火车站等到以琛和以玫就直冲了过来，以琛训她行李就扔在那里也不怕人拎走，默笙吐了吐舌头，笑说他们兄妹怎么一点不象，一个这么凶，一个这么温柔，是不是一个象爹，一个象妈啊？以玫看她还不知道自己和以琛并不是亲兄妹，觉得默笙和以琛并没有那么亲密。默笙总是对以玫很好，但以玫因为喜欢以琛所以总是对默笙亲热不起来，她总是自欺欺人地告诉自己以琛对默笙总是凶巴巴的，也许两人的感情并没有那么好的。直到有一天，她亲眼看到以琛和默笙激吻，她终于决定勇敢面对自己的心，她不想再这么等下去，等有一天以琛喜欢上她。她约出默笙，跟她坦白自己和以琛并不是兄妹，是自己的父母收养了以琛，她爱以琛，不想再这么偷偷摸摸地爱，她问默笙自问是不是抵得过自己和以琛多年青梅竹马的感情吗？受了刺激的默笙决定向以琛问清楚。同一天，默笙的父亲约了以琛见面。";
			System.out.println(request.getBytes().length);
//			byte[] compressed_1 = Snappy.compress(request.getBytes("UTF-8"));
//			System.out.println(compressed_1.length);
//			byte[] uncompressed_1 = Snappy.uncompress(compressed_1);
//			System.out.println(uncompressed_1.length);
//
//
//			long beginTime = System.currentTimeMillis();
//			for(int i = 0;i<100000;i++){
//				byte[] compressed = Snappy.compress(request.getBytes("UTF-8"));
//				Snappy.uncompress(compressed);
//			}
//
//			System.out.println((System.currentTimeMillis() - beginTime));
			

	}

}
