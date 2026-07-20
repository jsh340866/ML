package com.example.demo.Domain.Common.Repository;

import com.example.demo.Domain.Common.Entity.Signature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
// [수정] Signature 엔티티의 @Id 타입은 byte[] 이므로 ID 제네릭도 byte[] 로 일치 (기존 Byte 는 불일치)
public interface SignatureRepository extends JpaRepository<Signature,byte[]> {
}

