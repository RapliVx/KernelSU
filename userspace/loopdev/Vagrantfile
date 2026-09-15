Vagrant.configure("2") do |config|
  config.vm.box = "ubuntu/focal64"
  config.vm.provision "shell",
    inline: <<-EOS
      curl https://sh.rustup.rs -sSf | sh -s -- -y
      apt update && apt install -y gcc clang
      fallocate -l 128M /tmp/disk.img
      mv /tmp/disk.img /vagrant/
EOS
end
