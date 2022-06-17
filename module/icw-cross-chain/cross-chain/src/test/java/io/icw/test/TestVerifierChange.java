package io.icw.test;

import io.icw.base.data.Transaction;
import io.icw.core.crypto.HexUtil;
import io.icw.core.exception.NulsException;
import io.icw.core.log.Log;
import io.icw.crosschain.base.model.bo.txdata.VerifierChangeData;

/**
 * @Author: zhoulijun
 * @Time: 2020/10/12 17:05
 * @Description: 功能描述
 */
public class TestVerifierChange {
    public static void main(String[] args) throws NulsException {
        String hex = "18006f24555f002b09000001264e45525645657062363937776a4c6438676638426556426d5564666273705644777a4e61325800fd2d062102ae22c8f0f43081d82fcca1eae4488992cdb0caa9c902ba7cbfa0eacc1c6312f0463044022001f46a5e2e9aba6eee86771fe0848c93229bc70c2401c83271f4821986ebb9930220673779bf69e695e61e723132fd0ee6fdb9f7e1dbb6d79c6e8846d092fe909f8c210308ad97a2bf08277be771fc5450b6a0fa26fbc6c1e57c402715b9135d5388594b453043021f7a71d7453eac5778ee90ebb8eb9cbf6c597e4185de315d64d53318bf2713f702206769db869bb363b82fabdaf83c80aeaf4d1694bc41f3a0c19db7c83340a5c849210351a8fc85a6c475b102f3fe5bd2479c1d08e58237463f6c6ccf84e95ad396b7834730450221009e08dd33246afd609613041cf8278604adad38854748d450eddf2c5dd53e65b002202f989f6f8e4ae993bf7cdf491252eac3f4fa67ea254833ef44abf2786588b4f22102b5a63aafc3c4750063799ed2bc592f23969ac8a5692133fbde0d1a9eefb4dd66463044022008bddca9d9d4f1cb5c25cf0c558cfe07a24cf95bb3dfacd090fa1a473715ecac022000d2f81e6f0c6f5b59b6543ac3f6bb28afcc7b4d8454c91a7868a007cd27dda32102c8ab66541215350c4e82073c825d0d96dfe21aed1acfca3bdd91ac4d48cb3499473045022100f374de76ecebfde2276432efbbff820e887aa26076403ce0a32280512a3b46b902203ac03fecd57e19376bfca3f9cb1116889c0eaa84e9fc3ff9a5428ee47a23763921020c60dd7e0016e174f7ba4fc0333052bade8c890849409de7b6f3d26f0ec645284630440220409728c5643f57723acf0f24a46ad82f73270d3bd1cc5a5129dc6dc345daa8e902207c4515cba7c3f1af577c3ab78ac08151d7e52ff36e2a4551508d1c46a46a893e2102f9bdb6bf2d5e39cd826cd0712c861185b75b6028c5276df97adeb80706ef30b246304402201dd82262130195738cd757f2207f4a54332c0e20b081b25c0648451fc4a1673a02204eb2d0ef8c043decedd73f8aa1a5ea6d702ad4f2fe6e1a483f9725e978fcc79021035fe7599a7b39ad69fbd243aac7cfb93055f8f0827c6b08057874877cb890b80347304502210083513fb60072ac45eee987d01729493788e6386ff0636a7660236f152ca85a0c0220386cec322cfe1839062563f3cb09f07f685de6e127e557c39008ec6f96104728210338c67449adfbadaca769da9f1ff914e69afcc4a2ae09ff46104d524f711a1b4c473045022100b9ded54e79efeda199394834b79f73abf9813fa45f469f80c3fe575fb22808c40220544b59afdd0e28dcf420ec09d738ee0ef1667db2b395302469e6ae92159282e12102ac31c213b1dc1d2fd55d7751326b4f07b4a5b4ecb2ce3f214cafb7832fd211b9463044022064741638d258c5c5100561427b6334f96027cec68e4aea934048021afcef0cc80220381265cde521193e5ffa3e126b9699dac6bc707d9cc5d2df7dbbdde4361b8b5a2103ac396ab4bc360610058d04940c879e0da57ea1b4a541b75df6989a6c3d5081c9473045022100ddee535262581674088e3fe2da123faeb5bf8d84075451555b0dc06fd479a1cc02201c27828127d363dd295a248478536ed9aa7212fcf2659a6df84b8f70884f666e2102dda7bf54b7843aef842222f5c79405ca91313ac8c59296cf7b38203c09b40ba8473045022100b40d5c055c61298514f347bd2990f00032f0835e082511d4fa4996e42f8ac48902201c2b355763026d72f6a5a8d9023cb9050298a310fe1edaddefa3e6954af3ead021035c77b3debd02fa04c5c1c9ac3597152f2918d22a76288f3185e8c790e328fb6d473045022100a18b091dfb036083ed1b599c49e1a10df94f4453f11c796e45d18bc9145d0fe302202f38fae4f5090354ae48e2137a710fffb9261c33cf5e323bc3e27c14fd2608122103c363f44196aa1a57ef7e14c19845acad721c9eefd837dacdf3fe3af1ba08ee2146304402204e81cd5c85e2147f0b4539de4eaba0655cd10f8561d2be45a2bd545078e47d9802205f0b5bf79776bec51c36fe4561716eb70ef41c28f41af8294682a8ddd4ec906f21030d511912e9f1a953e5c8f1af3d4d59890acd0bc9472f40bf6d5bdfed2c4934bd46304402205c4a27b5df05daf6805139a82555394a0405ac910ab86d08bf7cf92d9cfe438f02205fa790d88dc7fe0d07cd251f7f3157c5569aa60a1d1241d2f55fa96caaca0803";
        Transaction tx = new Transaction();
        tx.parse(HexUtil.decode(hex),0);
        VerifierChangeData verifierChangeData = new VerifierChangeData();
        verifierChangeData.parse(tx.getTxData(),0);
        Log.info("{}",verifierChangeData);
    }

}
