#!/bin/sh -eux

apt-get -qq update
apt-get install --no-install-recommends -qq -y jq libatomic1 bats git wget shellcheck

# install bats
git clone https://github.com/bats-core/bats-core.git
cd bats-core
./install.sh /usr/local
