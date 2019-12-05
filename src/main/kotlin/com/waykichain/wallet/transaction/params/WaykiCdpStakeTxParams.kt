package com.waykichain.wallet.transaction.params

import com.waykichain.wallet.encode.HashWriter
import com.waykichain.wallet.encode.cdpHash
import com.waykichain.wallet.encode.encodeInOldWay
import com.waykichain.wallet.transaction.WaykiTxType
import org.bitcoinj.core.ECKey
import org.bitcoinj.core.Sha256Hash
import org.bitcoinj.core.Utils
import org.bitcoinj.core.VarInt

/**
 * srcRegId: (regHeight-regIndex PubKeyHash)
 * destAddr: 20-byte PubKeyHash
 * fee Minimum 0.001 wicc
 */
class WaykiCdpStakeTxParams(nValidHeight: Long, fees: Long = 1000L, val userId: String, var cdpTxid: String? = cdpHash,var feeSymbol: String, val assetMap:Map<String,Long>,val sCoinSymbol: String,
                           val sCoinToMint: Long) :
        BaseSignTxParams(nValidHeight, fees, WaykiTxType.TX_CDPSTAKE, 1) {

    private var userPubKey:ByteArray?=null
    override fun getSignatureHash(pubKey:String?): ByteArray {
        this.userPubKey=Utils.HEX.decode(pubKey)
        val ss = HashWriter()
        val cdpTxHex = Utils.HEX.decode(cdpTxid).reversedArray()
        ss.add(VarInt(nVersion).encodeInOldWay())
                .add(nTxType.value)
                .add(VarInt(nValidHeight).encodeInOldWay())
                .writeUserId(userId, userPubKey)
                .add(feeSymbol)
                .add(VarInt(fees).encodeInOldWay())
                .add(cdpTxHex)
                .add(VarInt(assetMap?.size.toLong()).encodeInOldWay())
                .addCdpAssets(assetMap)
                .add(sCoinSymbol)
                .add(VarInt(sCoinToMint).encodeInOldWay())
        val hash = Sha256Hash.hashTwice(ss.toByteArray())
        return hash
    }

    override fun serializeTx(signature:ByteArray): String {
        assert(signature != null)
        val sigSize = signature!!.size
        val cdpTxHex = Utils.HEX.decode(cdpTxid).reversedArray()
        val ss = HashWriter()
        ss.add(VarInt(nTxType.value.toLong()).encodeInOldWay())
                .add(VarInt(nVersion).encodeInOldWay())
                .add(VarInt(nValidHeight).encodeInOldWay())
                .writeUserId(userId, this.userPubKey)
                .add(feeSymbol)
                .add(VarInt(fees).encodeInOldWay())
                .add(cdpTxHex)
                .add(VarInt(assetMap.size.toLong()).encodeInOldWay())
                .addCdpAssets(assetMap)
                .add(sCoinSymbol)
                .add(VarInt(sCoinToMint).encodeInOldWay())
                .writeCompactSize(sigSize.toLong())
                .add(signature)

        val hexStr = Utils.HEX.encode(ss.toByteArray())
        return hexStr
    }
}