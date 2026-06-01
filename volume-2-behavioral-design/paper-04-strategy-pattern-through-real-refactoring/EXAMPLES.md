# Strategy Pattern Classification Examples

## Example 1 - Encryption

**Before**

```java
if(type.equals("AES"))
   ...

if(type.equals("DES"))
   ...

if(type.equals("BLOWFISH"))
   ...
```

**Pressure:** Behavior Variation

**After**

```java
EncryptionStrategy strategy;
strategy.encrypt(text);
```

## Example 2 - Payment Gateway

**Before**

```java
if(provider.equals("PAYPAL"))
   ...

if(provider.equals("STRIPE"))
   ...

if(provider.equals("RAZORPAY"))
   ...
```

**Pressure:** Behavior Variation

**After**

```java
PaymentStrategy strategy;
strategy.pay(amount);
```

## Example 3 - Pricing Engine

**Before**

```java
if(customer.isPremium())
   ...

if(customer.isEmployee())
   ...

if(customer.isPartner())
   ...
```

**Pressure:** Behavior Variation

**After**

```java
PricingStrategy strategy;
strategy.calculatePrice(product);
```

## Architect Rule

Strategy Pattern becomes useful when:

- The caller remains the same.
- The behavior changes.
