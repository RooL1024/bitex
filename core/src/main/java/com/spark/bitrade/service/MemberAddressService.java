package com.spark.bitrade.service;

import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.CommonStatus;
import com.spark.bitrade.dao.CoinDao;
import com.spark.bitrade.dao.MemberAddressDao;
import com.spark.bitrade.entity.Coin;
import com.spark.bitrade.entity.MemberAddress;
import com.spark.bitrade.pagination.Criteria;
import com.spark.bitrade.pagination.Restrictions;
import com.spark.bitrade.service.Base.BaseService;
import com.spark.bitrade.util.MessageResult;
import com.sparkframework.sql.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Zhang Jinwei
 * @date 2018年01月26日
 */
@Service
public class MemberAddressService extends BaseService {
    @Autowired
    private MemberAddressDao memberAddressDao;
    @Autowired
    private CoinDao coinDao;

    public MessageResult addMemberAddress(Long memberId, String address, String unit, String remark) {
        Coin coin = coinDao.findByUnit(unit);
        if (coin == null || coin.getCanWithdraw().equals(BooleanEnum.IS_FALSE)) {
            return MessageResult.error(600, "The currency does not support withdrawals");
        }
        MemberAddress memberAddress = new MemberAddress();
        memberAddress.setAddress(address);
        memberAddress.setCoin(coin);
        memberAddress.setMemberId(memberId);
        memberAddress.setRemark(remark);
        MemberAddress memberAddress1=memberAddressDao.saveAndFlush(memberAddress);
        if (memberAddress1!=null){
            return MessageResult.success();
        }else {
            return MessageResult.error( "failed");
        }
    }

    public MessageResult deleteMemberAddress(Long memberId,Long addressId){
        int is=memberAddressDao.deleteMemberAddress(new Date(), addressId, memberId);
        if (is>0){
            return MessageResult.success();
        }else {
            return MessageResult.error("failed");
        }
    }

    public Page<MemberAddress> pageQuery(int pageNo, Integer pageSize, long id,String unit) {
        Sort orders = Criteria.sortStatic("id.desc");
        PageRequest pageRequest = new PageRequest(pageNo, pageSize, orders);
        Criteria<MemberAddress> specification = new Criteria<>();
        specification.add(Restrictions.eq("memberId",id,false));
        specification.add(Restrictions.eq("status", CommonStatus.NORMAL, false));
        specification.add(Restrictions.eq("coin.unit",unit,false));
        return memberAddressDao.findAll(specification, pageRequest);
    }

    public List<Map<String,String>> queryAddress(long userId,String coinId)  {
        try {
            return new Model("member_address")
                    .field(" remark,address")
                    .where("member_id=? and coin_id=? and status=?", userId, coinId, CommonStatus.NORMAL.ordinal())
                    .select();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public List<MemberAddress> findByMemberIdAndAddress(long userId,String address){
        return memberAddressDao.findAllByMemberIdAndAddressAndStatus(userId,address,CommonStatus.NORMAL);
    }

    public List<MemberAddress> findByMemberIdAndCoinAndAddress(long userId,Coin coin ,String address,CommonStatus status){
        return  memberAddressDao.findByMemberIdAndCoinAndAddressAndStatus(userId,coin,address,status);
    }
}
